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
#import "PageTextInfo.h"
#import "NSString+Search.h"
#import "RangeObject.h"
#import "ImageSearchViewController.h"

@interface ImageViewController ()
- (void)buildPageHeads;
- (void)toggleConfigView;
- (void)movePageToCurrent:(BOOL)isLeft;
- (void)loadSinglePageInfo;
- (PageTextInfo *)loadPageTextInfo;
- (double)loadRatio;
@end

@implementation ImageViewController

@synthesize configView;
@synthesize titleLabel;
@synthesize tiledScrollViewContainer;
@synthesize documentId;
@synthesize tiledScrollView;

+ (ImageViewController *)createViewController:(int)documentId page:(int)page {
    ImageViewController *ret = [[[ImageViewController alloc] initWithNibName:[IUIViewController buildNibName:@"Image"] bundle:nil] autorelease];
    [ret setLandspace];
    ret.documentId = documentId;
    [ret setIndexByPage:page];
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
    BOOL isLand = UIDeviceOrientationIsLandscape( [UIDevice currentDevice].orientation );
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

- (void)selectSearchResult:(int)page range:(NSRange)range {
    int next = [self calcIndexByPage:page];
    if ( next != currentIndex ) {
        BOOL isLeft = next > currentIndex;
        currentIndex = next;
        [self movePageToCurrent:isLeft];
    }
    
    NSMutableArray *regions = [NSMutableArray arrayWithCapacity:0];
    PageTextInfo *info = [self loadPageTextInfo];
    
    for ( int index = 0 ; index < range.length ; index++ ) {
        [regions addObject:[info.regions objectAtIndex:(range.location + index)]];
    }
    
    [self.tiledScrollView drawMarker:regions ratio:[self loadRatio]];

    [self toggleConfigView];
    
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

- (IUIViewController *)createViewController {
    return [ImageViewController createViewController:self.documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]];
}

- (TiledScrollView *)buildContentView {
    CGRect frame = self.tiledScrollViewContainer.bounds;
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
    
    int maxLebel = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? MAX_LEVEL - 1 : ([[UIScreen mainScreen] scale] == 2.0 ? MAX_LEVEL - 1 : MAX_LEVEL);

    TiledScrollView *ret = [[[TiledScrollView alloc] initWithFrame:frame] autorelease];
    
    ret.dataSource = self;
    ret.tileContainerView.delegate = self;
    ret.maximumResolution = maxLebel;
    ret.minimumResolution = 0;

    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        ret.tileSize = CGSizeMake( w , h );
    } else {
        if ( [[UIScreen mainScreen] scale] == 2.0 ) {
            ret.tileSize = CGSizeMake( w / 2 , h / 2 );
        } else {
            ret.tileSize = CGSizeMake( w , h );
        }
    }
    ret.clipsToBounds = NO;
    ret.baseScale = baseScale;
    
    ret.bouncesZoom = NO;
    
    // change the content size and reset the state of the scroll view
    // to avoid interactions with different zoom scales and resolutions.
    [ret reloadDataWithNewContentSize:ret.frame.size];
    ret.contentOffset = CGPointZero;
    // choose maximum scale so image width fills screen
    ret.maximumZoomScale = pow( 2 , maxLebel );
    
    return ret;
}

- (void)saveHistory {
    HistoryObject *history = [[HistoryObject new] autorelease];
    history.documentId = documentId;
    history.page = [[pageHeads objectAtIndex:currentIndex] intValue];
    
    [FileUtil saveHistory:history];
}

- (void)viewDidLoad {
    [super viewDidLoad];

    self.titleLabel.text = [FileUtil toc:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]].text;
    
    self.tiledScrollView = [self buildContentView];
    [self.tiledScrollViewContainer addSubview:self.tiledScrollView];
    
    [self saveHistory];
}

- (void)dealloc {
    [configView release];
    [tiledScrollView release];
    [prevTiledScrollView release];
    [singlePageInfo release];
    [pageHeads release];
    [isSinglePage release];
    
    [super dealloc];
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
    double dst = self.configView.alpha > 0 ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self.configView cache:NO];
    
    self.configView.alpha = dst;
    
    [UIView commitAnimations];
}

#pragma mark load

- (void)loadSinglePageInfo {
    singlePageInfo = [[[NSString stringWithData:[FileUtil read:[NSString stringWithFormat:@"%d/singlePageInfo.json" , documentId]]] JSONValue] retain];
}

- (PageTextInfo *)loadPageTextInfo {
    return [PageTextInfo objectWithDictionary:
            [[NSString stringWithData:
              [FileUtil read:
               [NSString stringWithFormat:@"%d/texts/%d.info" , documentId, [[pageHeads objectAtIndex:currentIndex] intValue]]]] JSONValue]];
}

- (double)loadRatio {
    return [[[[NSString stringWithData:
               [FileUtil read:
                [NSString stringWithFormat:@"%d/info.json" , documentId]]] JSONValue] objectForKey:@"ratio"] doubleValue];
}

#pragma mark TiledScrollViewDataSource method

- (UIView *)tiledScrollView:(TiledScrollView *)tiledScrollView tileForRow:(int)row column:(int)column resolution:(int)resolution {
    resolution += [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
    int delta = 0;
    if ( isLandscape && ![[isSinglePage objectAtIndex:currentIndex] boolValue] ) {
        if ( column >= pow( 2 , resolution ) ) {
            column -= pow( 2 , resolution );
        } else {
            delta += 1;
        }
    }
    
    if ( row < 0 || column < 0 || row >= pow( 2 , resolution ) || column >= pow( 2 , resolution ) ) {
        return nil;
    }

    NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    NSString *fileName = [NSString stringWithFormat:@"%d/images/%@-%d-%d-%d-%d.jpg" , documentId , type ,
                          ([[pageHeads objectAtIndex:currentIndex] intValue] + delta) , resolution , column , row];
    if ( [FileUtil exists:fileName] ) {
        UIImageView *tile = [[[UIImageView alloc] initWithImage:[UIImage imageWithContentsOfFile:[FileUtil getFullPath:fileName]]] autorelease];
        tile.tag = 1;
        return tile;
    } else {
        // the scroll view will handle settiag the tile's frame, so we don't have to worry about it
        UIRemoteImageView *tile = [[[UIRemoteImageView alloc ] initWithFrame:CGRectZero] autorelease];
        tile.delegate = self;
        
        [tile load:documentId page:([[pageHeads objectAtIndex:currentIndex] intValue] + delta) level:resolution px:column py:row];
        
        nRequestTile++;
        
        return tile;
    }
}

- (void)resetLoad {
    nRequestTile = 0;
}

- (void)movePageToCurrent:(BOOL)isLeft {
    self.titleLabel.text = [FileUtil toc:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]].text;
    
    prevTiledScrollView = [[tiledScrollView retain] autorelease];
    [self.tiledScrollView removeFromSuperview];
    
    self.tiledScrollView = [self buildContentView];
    [self.tiledScrollViewContainer addSubview:self.tiledScrollView];
    
    CATransition *animation = [CATransition animation];
    animation.duration = 0.5;
    animation.type = kCATransitionPush;
    animation.subtype = isLeft ? kCATransitionFromLeft : kCATransitionFromRight;
    animation.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    animation.delegate = self;
    
    [self.view.layer addAnimation:animation forKey:nil];
    
    [self saveHistory];
}

- (void)movePage:(BOOL)isLeft {
    int delta = isLeft ? 1 : -1;
    
    if ( currentIndex + delta < 0 || currentIndex + delta >= [pageHeads count] ) {
        return;
    }
    
    currentIndex += delta;
    
    [self movePageToCurrent:isLeft];
}

- (void)beginDragging {
    if ( self.configView.alpha > 0 ) {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:0.3];
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self.configView cache:NO];
        
        self.configView.alpha = 0;
        
        [UIView commitAnimations];
    }
}

#pragma mark CAAnimationDelegate

- (void)animationDidStop:(CAAnimation *)theAnimation finished:(BOOL)flag {
    prevTiledScrollView = nil;
}

#pragma mark UIRemoteImageViewDelegate

- (void) remoteImageViewDidFinish {
    nRequestTile--;
    
    if ( nRequestTile == 0 ) {
        [self.tiledScrollView releasePending];
    }
}

#pragma mark TapDetectingViewDelegate 

- (void)tapDetectingView:(TapDetectingView *)view gotSingleTapAtPoint:(CGPoint)tapPoint {
    [self toggleConfigView];
}

- (void)tapDetectingView:(TapDetectingView *)view gotDoubleTapAtPoint:(CGPoint)tapPoint {
}

- (void)tapDetectingView:(TapDetectingView *)view gotTwoFingerTapAtPoint:(CGPoint)tapPoint {
}

@end
