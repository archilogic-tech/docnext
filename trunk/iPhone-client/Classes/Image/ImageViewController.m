//
//  ImageViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "ImageViewController.h"
#import "UIRemoteImageView.h"
#import "Const.h"
#import "FileUtil.h"
#import "NSString+Data.h"
#import "JSON.h"
#import "NSString+Search.h"
#import "RangeObject.h"
#import "ImageSearchViewController.h"
#import "UIBalloon.h"
#import "SeparationHolder.h"

@interface ImageViewController ()
- (TiledScrollView *)buildContentView;
- (void)saveHistory;
- (int)calcIndexByPage:(int)page;
- (void)buildPageHeads;
- (void)toggleConfigView;
- (void)movePageToCurrent:(BOOL)isLeft;
- (void)loadSinglePageInfo;
@end

@implementation ImageViewController

@synthesize configView;
@synthesize titleLabel;
@synthesize tiledScrollViewContainer;
@synthesize window;
@synthesize documentId;

#pragma mark lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
    
    tapDetector = [TapDetector new];
    tapDetector.delegate = self;
    
    overlayManager = [OverlayManager new];
    
    titleLabel.text = [FileUtil toc:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]].text;
    
    tiledScrollView = [[self buildContentView] retain];
    [tiledScrollViewContainer addSubview:tiledScrollView];
    
    [self saveHistory];
    
    window.touchesObserver = self;

    imageFetchQueue = [NSOperationQueue new];
    [imageFetchQueue setMaxConcurrentOperationCount:1];

    [overlayManager setParam:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue] size:tiledScrollView.frame.size];
}

- (void)dealloc {
    window.touchesObserver = nil;
    
    [configView release];
    [tiledScrollView release];
    [tiledScrollViewContainer release];
    [markerView release];
    [balloonContainerView release];
    [tapDetector release];
    [prevTiledScrollView release];
    [singlePageInfo release];
    [pageHeads release];
    [isSinglePage release];
    [overlayManager release];
    [imageFetchQueue release];
    
    [super dealloc];
}

#pragma mark public

+ (ImageViewController *)createViewController:(UIInterfaceOrientation)orientation docId:(int)documentId page:(int)page
                                       window:(UITouchAwareWindow *)window {
    ImageViewController *ret = [[[ImageViewController alloc] initWithNibName:
                                 [IUIViewController buildNibName:@"Image" orientation:orientation] bundle:nil] autorelease];
    [ret setLandspace:orientation];
    ret.documentId = documentId;
    [ret setIndexByPage:page];
    ret.window = window;
    return ret;
}

- (IBAction)homeButtonClick:(id)sender {
    [parent showHome:YES];
}

- (IBAction)tocViewButtonClick:(id)sender {
    [parent showTOC:documentId prevPage:[[pageHeads objectAtIndex:currentIndex] intValue]];
}

- (IBAction)thumbnailViewButtonClick:(id)sender {
    [parent showThumbnail:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]];
}

- (IBAction)bookmarkViewButtonClick:(id)sender {
    [parent showBookmark:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]];
}

- (IBAction)textViewButtonClick:(id)sender {
    [parent showText:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]];
}

- (IBAction)tweetButtonClick:(id)sender {
    NSURL *url = [NSURL URLWithString:[[NSString stringWithFormat:@"http://twitter.com/home?status=Sample tweet"]
                                       stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    [[UIApplication sharedApplication] openURL:url];
}

- (IBAction)searchButtonClick:(id)sender {
    BOOL isLand = UIInterfaceOrientationIsLandscape( self.interfaceOrientation );
    
    if ( isLand ) {
        [[[[UIAlertView alloc] initWithTitle:@"Search function is disabled currently on landscape orientation"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
        return;
    }
    
    NSString *orientation = isLand ? @"-land" : @"";
    searchViewController = [[ImageSearchViewController alloc]
                            initWithNibName:[NSString stringWithFormat:@"ImageSearchViewController%@" , orientation] bundle:nil];
    searchViewController.parent = self;
    searchViewController.docId = documentId;
    
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        popover = [[UIPopoverController alloc] initWithContentViewController:searchViewController];
        popover.popoverContentSize = isLand ? CGSizeMake(480, 320) : CGSizeMake(320, 480);
        [popover presentPopoverFromRect:((UIView *)sender).frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    } else {
        [self.view addSubview:searchViewController.view];
    }
}

- (void)selectSearchResult:(int)page ranges:(NSArray *)ranges selectedIndex:(int)selectedIndex {
    int next = [self calcIndexByPage:page];
    if ( next != currentIndex ) {
        BOOL isLeft = next > currentIndex;
        currentIndex = next;
        [self movePageToCurrent:isLeft];
    }

    [overlayManager showSearchResult:ranges selectedIndex:selectedIndex];

    [self toggleConfigView];

    [self cancelSearch];
}

- (void)cancelSearch {
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        [popover dismissPopoverAnimated:YES];
        [popover release];
        popover = nil;
    } else {
        [searchViewController.view removeFromSuperview];
    }
    
    [searchViewController release];
    searchViewController = nil;
}

- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation {
    return [ImageViewController createViewController:orientation docId:documentId
                                                page:[[pageHeads objectAtIndex:currentIndex] intValue] window:window];
}

#pragma mark private

- (int)calcIndexByPage:(int)page {
    for ( int index = 0 ; index < [pageHeads count] ; index++ ) {
        int head = [[pageHeads objectAtIndex:index] intValue];
        if ( head == page ) {
            return index;
        }
        if ( head > page ) {
            return index - 1;
        }
    }
    
    return [pageHeads count] - 1;
}

- (void)setIndexByPage:(int)page {
    totalPage = [FileUtil pages:documentId];
    [self loadSinglePageInfo];
    [self buildPageHeads];
    
    currentIndex = [self calcIndexByPage:page];
}

- (void)addOverlay:(TiledScrollView *)view {
    [markerView release];
    markerView = [[MarkerView alloc] initWithFrame:view.frame];
    [view.zoomableContainerView addSubview:markerView];
    
    [balloonContainerView release];
    balloonContainerView = [[UIView alloc] initWithFrame:view.frame];
    balloonContainerView.userInteractionEnabled = NO;
    [view addSubview:balloonContainerView];
}

- (void)setExternalDependent:(TiledScrollView *)view {
    tapDetector.basisView = view.zoomableContainerView;
    overlayManager.scrollView = view;
    overlayManager.markerView = markerView;
    overlayManager.balloonContainerView = balloonContainerView;
}

- (TiledScrollView *)buildContentView {
    CGRect frame = tiledScrollViewContainer.bounds;
    float w = frame.size.width;
    float h = frame.size.height;
    float baseScale = 1.0f;
    if ( isLandscape ) {
        w /= 2;
        baseScale = 0.5;
        
        if ( [[isSinglePage objectAtIndex:currentIndex] boolValue] ) {
            frame = CGRectMake(w / 2, 0, w, frame.size.height);
        }
    }
    
    int maxLebel = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad || [[UIScreen mainScreen] scale] == 2.0 ? MAX_LEVEL - 1 : MAX_LEVEL;
    
    TiledScrollView *ret = [[[TiledScrollView alloc] initWithFrame:frame] autorelease];
    
    float scale = [[UIScreen mainScreen] scale];
    ret.tileSize = CGSizeMake(w / scale, h / scale);
    
    ret.dataSource = self;
    ret.maximumResolution = maxLebel;
    ret.clipsToBounds = NO;
    ret.bouncesZoom = NO;
    ret.baseScale = baseScale;
    
    [ret setContentSizeProperty:ret.frame.size];
    ret.contentOffset = CGPointZero;
    ret.maximumZoomScale = pow( 2 , maxLebel );
    
    [self addOverlay:ret];
    [self setExternalDependent:ret];

    return ret;
}

- (void)saveHistory {
    HistoryObject *history = [[HistoryObject new] autorelease];
    history.documentId = documentId;
    history.page = [[pageHeads objectAtIndex:currentIndex] intValue];
    
    [FileUtil saveHistory:history];
}

- (BOOL)isSinglePage:(int)page {
    for ( NSString *value in singlePageInfo ) {
        if ( [value intValue] == page ) {
            return YES;
        }
    }
    
    return NO;
}

- (void)buildPageHeads {
    NSMutableArray *_pageHeads = [NSMutableArray arrayWithCapacity:0];
    NSMutableArray *_isSinglePage = [NSMutableArray arrayWithCapacity:0];
    
    for ( int page = 0 ; page < totalPage ; ) {
        [_pageHeads addObject:[NSNumber numberWithInt:page]];
        if ( isLandscape && ![self isSinglePage:page] && page + 1 < totalPage && ![self isSinglePage:(page + 1)] ) {
            [_isSinglePage addObject:[NSNumber numberWithBool:NO]];
            page += 2;
        } else {
            [_isSinglePage addObject:[NSNumber numberWithBool:YES]];
            page += 1;
        }
    }
    
    pageHeads = [_pageHeads retain];
    isSinglePage = [_isSinglePage retain];
}

- (void)toggleConfigView {
    double dst = configView.alpha > 0 ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
    configView.alpha = dst;
    
    [UIView commitAnimations];
}

- (void)movePageToCurrent:(BOOL)isLeft {
    titleLabel.text = [FileUtil toc:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]].text;
    
    prevTiledScrollView = tiledScrollView;
    [tiledScrollView removeFromSuperview];
    
    tiledScrollView = [[self buildContentView] retain];
    [tiledScrollViewContainer addSubview:tiledScrollView];
    
    CATransition *animation = [CATransition animation];
    animation.duration = 0.5;
    animation.type = kCATransitionPush;
    animation.subtype = isLeft ? kCATransitionFromLeft : kCATransitionFromRight;
    animation.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    animation.delegate = self;
    
    [self.view.layer addAnimation:animation forKey:nil];
    
    [self saveHistory];
    
    [overlayManager setParam:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue] size:tiledScrollView.frame.size];
}

#pragma mark load

- (void)loadSinglePageInfo {
    singlePageInfo = [[[NSString stringWithData:
                        [FileUtil read:
                         [NSString stringWithFormat:@"%d/singlePageInfo.json" , documentId]]] JSONValue] retain];
}

#pragma mark TiledScrollViewDataSource method

- (UIView *)tiledScrollViewGetTile:(TiledScrollView *)tiledScrollView row:(int)row column:(int)column resolution:(int)resolution {
    resolution += [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
    int page = [[pageHeads objectAtIndex:currentIndex] intValue];
    if ( isLandscape && ![[isSinglePage objectAtIndex:currentIndex] boolValue] ) {
        if ( column >= pow( 2 , resolution ) ) {
            column -= pow( 2 , resolution );
        } else {
            page++;
        }
    }
    
    if ( row < 0 || column < 0 || row >= pow( 2 , resolution ) || column >= pow( 2 , resolution ) ) {
        return nil;
    }

    NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    NSString *fileName = [NSString stringWithFormat:@"%d/images/%@-%d-%d-%d-%d.jpg", documentId, type, page,
                          resolution, column, row];
    if ( [FileUtil exists:fileName] ) {
        UIImageView *tile = [[[UIImageView alloc] initWithImage:
                              [UIImage imageWithContentsOfFile:[FileUtil getFullPath:fileName]]] autorelease];

        tile.tag = TiledScrollViewTileLocal;
        
        return tile;
    } else {
        UIRemoteImageView *tile = [[[UIRemoteImageView alloc ] initWithFrame:CGRectZero] autorelease];
        
        [tile load:imageFetchQueue docId:documentId page:page level:resolution px:column py:row];
        
        return tile;
    }
}

- (void)tiledScrollViewDidSlide:(TiledScrollViewSlideDirection)direction {
    BOOL isLeft = direction == TiledScrollViewSlideDirectionLeft;
    int delta = isLeft ? 1 : -1;
    
    if ( currentIndex + delta < 0 || currentIndex + delta >= [pageHeads count] ) {
        return;
    }
    
    currentIndex += delta;
    
    [self movePageToCurrent:isLeft];
}

- (void)tiledScrollViewScaleChanging:(float)scale {
    [overlayManager applyScaleView:scale];
}

#pragma mark CAAnimationDelegate

- (void)animationDidStop:(CAAnimation *)theAnimation finished:(BOOL)flag {
    prevTiledScrollView = nil;
    [prevTiledScrollView release];
}

#pragma mark TapDetectorDelegate 

- (void)tapDetectorGotSingleTapAtPoint:(CGPoint)tapPoint {
    [self toggleConfigView];
}

- (void)tapDetectorGotDoubleTapAtPoint:(CGPoint)tapPoint {
    float scale = tiledScrollView.zoomScale;
    float toScale;
    if ( scale < tiledScrollView.maximumZoomScale ) {
        toScale = MIN( scale * 2 , tiledScrollView.maximumZoomScale );
    } else {
        toScale = 1;
    }

    CGSize boundsSize = tiledScrollView.bounds.size;
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationBeginsFromCurrentState:YES];
	[UIView setAnimationDuration:0.3];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:tiledScrollView cache:YES];

    tiledScrollView.zoomScale = toScale;
    tiledScrollView.contentOffset = CGPointMake(MAX(MIN(tapPoint.x * toScale - boundsSize.width / 2 ,
                                                             boundsSize.width * (toScale - 1)) , 0),
                                                     MAX(MIN(tapPoint.y * toScale - boundsSize.height / 2 ,
                                                             boundsSize.height * (toScale - 1)) , 0));
    [overlayManager applyScaleView:toScale];

    [UIView commitAnimations];
}

- (void)tapDetectorGotSingleLongTapAtPoint:(CGPoint)tapPoint {
    BOOL isLand = UIInterfaceOrientationIsLandscape( self.interfaceOrientation );
    
    if ( isLand ) {
        [[[[UIAlertView alloc] initWithTitle:@"Selecting function is disabled currently on landscape orientation"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
        return;
    }

    [overlayManager selectNearest:tapPoint];
}

#pragma mark TouchObserver

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    if ( [[[touches anyObject] view] isDescendantOfView:tiledScrollView] ) {
        [tapDetector touchesBegan:touches withEvent:event];
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    [tapDetector touchesMoved:touches withEvent:event];
    
    if ( [[[touches anyObject] view] isDescendantOfView:tiledScrollView] ) {
        if ( configView.alpha > 0 ) {
            [self toggleConfigView];
        }
    }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    if ( [[[touches anyObject] view] isDescendantOfView:tiledScrollView] ) {
        [tapDetector touchesEnded:touches withEvent:event];
    }
}

@end
