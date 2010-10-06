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
#import "HighlightObject.h"
#import "ObjPoint.h"

@interface ImageViewController ()
- (TiledScrollView *)buildContentView;
- (void)saveHistory;
- (int)calcIndexByPage:(int)page;
- (void)buildPageHeads;
- (void)toggleConfigView;
- (void)toggleHighlightMenu;
- (void)toggleHighlightCommentMenu;
- (void)movePageToCurrent:(BOOL)isLeft;
- (UIColor *)highlightColor:(int)index;
- (void)addAnnotations;
- (void)loadSinglePageInfo;
- (void)loadHighlights;
- (void)saveHighlights;
- (NSArray *)loadAnnotations;
- (void)saveFreehand;
- (void)loadFreehand;
@end

@implementation ImageViewController

@synthesize configView;
@synthesize freehandSwitch = _freehandSwitch;
@synthesize titleLabel;
@synthesize tiledScrollViewContainer;
@synthesize selectionMenuView;
@synthesize highlightMenuView;
@synthesize highlightCommentMenuView;
@synthesize highlightCommentTextField;
@synthesize window;
@synthesize documentId;

#pragma mark lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
    
    tapDetector = [TapDetector new];
    tapDetector.delegate = self;
    
    overlayManager = [OverlayManager new];
    overlayManager.delegate = self;
    
    highlights = [[NSMutableDictionary dictionaryWithCapacity:0] retain];
    
    titleLabel.text = [FileUtil toc:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]].text;
    
    tiledScrollView = [[self buildContentView] retain];
    [tiledScrollViewContainer addSubview:tiledScrollView];
    
    [self saveHistory];
    
    window.touchesObserver = self;

    imageFetchQueue = [NSOperationQueue new];
    [imageFetchQueue setMaxConcurrentOperationCount:1];

    [overlayManager setParam:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue] size:tiledScrollView.frame.size];
    
    [self loadHighlights];
    [self addAnnotations];
    [self loadFreehand];
}

- (void)dealloc {
    window.touchesObserver = nil;
    
    [configView release];
    [_freehandSwitch release];
    [tiledScrollView release];
    [tiledScrollViewContainer release];
    [selectionMenuView release];
    [highlightMenuView release];
    [highlightCommentMenuView release];
    [highlightCommentTextField release];
    [markerView release];
    [_freehandView release];
    [balloonContainerView release];
    [tapDetector release];
    [prevTiledScrollView release];
    [singlePageInfo release];
    [pageHeads release];
    [isSinglePage release];
    [overlayManager release];
    [highlights release];
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

- (IBAction)freehandUndoClick {
    [_freehandView undo];
    [self saveFreehand];
}

- (IBAction)freehandClearClick {
    [_freehandView clear];
    [self saveFreehand];
}

- (IBAction)freehandSwitchChanged {
    tiledScrollView.scrollEnabled = !_freehandSwitch.on;
    _freehandView.userInteractionEnabled = _freehandSwitch.on;
    
    [self saveFreehand];
}

- (IBAction)copyButtonClick {
    NSString *text = [FileUtil imageText:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]];
    [[UIPasteboard generalPasteboard] setString:[text substringWithRange:[overlayManager selection]]];
    
    [self toggleConfigView];
    [overlayManager clearSelection];
}

- (IBAction)highlightButtonClick {
    currentHighlightSerial = [overlayManager showHighlight:[overlayManager selection] color:[self highlightColor:0] selecting:YES];
    
    HighlightObject *highlight = [[HighlightObject new] autorelease];
    highlight.location = [overlayManager selection].location;
    highlight.length = [overlayManager selection].length;
    highlight.color = 0;
    highlight.text = @"";
    
    [highlights setObject:highlight forKey:[NSNumber numberWithInt:currentHighlightSerial]];
    
    [self saveHighlights];
    
    [self toggleConfigView];
    [overlayManager clearSelection];
    
    [self toggleHighlightMenu];
}

- (IBAction)highlightCommentButtonClick {
    HighlightObject *highlight = [highlights objectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    highlightCommentTextField.text = highlight.text;
    
    [self toggleHighlightCommentMenu];
}

- (IBAction)highlightCommentApplyButtonClick {
    [highlightCommentTextField resignFirstResponder];
    
    [overlayManager changeHighlightComment:currentHighlightSerial text:highlightCommentTextField.text];
    [overlayManager applyScaleView:tiledScrollView.zoomScale];
    
    HighlightObject *highlight = [highlights objectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    highlight.text = highlightCommentTextField.text;
    [self saveHighlights];
    
    [self toggleHighlightCommentMenu];
}

- (IBAction)highlightChangeColorClick:(UIButton *)sender {
    [overlayManager changeHighlightColor:currentHighlightSerial color:[self highlightColor:sender.tag]];

    HighlightObject *highlight = [highlights objectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    highlight.color = sender.tag;
    [self saveHighlights];
    
    [self toggleHighlightMenu];
    if ( highlightCommentMenuView.alpha > 0 ) {
        [self toggleHighlightCommentMenu];
    }
}

- (IBAction)highlightDeleteClick {
    [overlayManager deleteHighlight:currentHighlightSerial];

    [highlights removeObjectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    [self saveHighlights];
    
    currentHighlightSerial = -1;
    
    
    [self toggleHighlightMenu];
    if ( highlightCommentMenuView.alpha > 0 ) {
        [self toggleHighlightCommentMenu];
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
    
    [_freehandView release];
    _freehandView = [[UIFreehandView alloc] initWithFrame:view.frame];
    _freehandView.delegate = self;
    [view.zoomableContainerView addSubview:_freehandView];
    
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
    if ( [overlayManager hasSelection] ) {
        selectionMenuView.alpha = dst;
    }
    if ( highlightMenuView.alpha > 0 ) {
        highlightMenuView.alpha = 0;
    }
    
    [UIView commitAnimations];
}

- (void)toggleHighlightMenu {
    double dst = highlightMenuView.alpha > 0 ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
    highlightMenuView.alpha = dst;
    
    [UIView commitAnimations];
}

- (void)toggleHighlightCommentMenu {
    double dst = highlightCommentMenuView.alpha > 0 ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
    highlightCommentMenuView.alpha = dst;
    
    [UIView commitAnimations];
}

- (void)addAnnotations {
    for ( NSDictionary *dict in [self loadAnnotations] ) {
        NSDictionary *action = [dict objectForKey:@"action"];
        NSString *actionType = [action objectForKey:@"action"];

        if ( [actionType compare:@"URI"] == NSOrderedSame ) {
            [overlayManager addURILink:[Region objectWithDictionary:[dict objectForKey:@"region"]] uri:[action objectForKey:@"uri"]];
        } else if ( [actionType compare:@"GoToPage"] == NSOrderedSame ) {
            [overlayManager addGoToPageLink:[Region objectWithDictionary:[dict objectForKey:@"region"]] page:[[action objectForKey:@"page"] intValue]];
        } else {
            assert(0);
        }
    }
}

- (void)movePageToCurrent:(BOOL)isLeft {
    titleLabel.text = [FileUtil toc:documentId page:[[pageHeads objectAtIndex:currentIndex] intValue]].text;

    [overlayManager clearSelection];

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
    [self loadHighlights];
    [self addAnnotations];
    [self loadFreehand];
}

- (UIColor *)highlightColor:(int)index {
    int COLORS[] = {0xff0000 , 0x00ff00 , 0x0000ff};
    
    int color = COLORS[ index ];
    
    return [UIColor colorWithRed:((color >> 16) & 0xff)
                           green:((color >> 8) & 0xff)
                            blue:(color & 0xff) alpha:0.5];
}

#pragma mark load

- (void)loadSinglePageInfo {
    singlePageInfo = [[[NSString stringWithData:
                        [FileUtil read:
                         [NSString stringWithFormat:@"%d/singlePageInfo.json" , documentId]]] JSONValue] retain];
}

- (void)loadHighlights {
    NSData *data = [FileUtil read:[NSString stringWithFormat:@"%d/%d.highlight.json" , documentId , [[pageHeads objectAtIndex:currentIndex] intValue]]];
    for ( NSDictionary *dic in [[NSString stringWithData:data] JSONValue] ) {
        HighlightObject *highlight = [HighlightObject objectWithDictionary:dic];
        
        int serial = [overlayManager showHighlight:[highlight range] color:[self highlightColor:highlight.color] selecting:NO];
        [overlayManager changeHighlightComment:serial text:highlight.text];
        
        [highlights setObject:highlight forKey:[NSNumber numberWithInt:serial]];
    }
}

- (void)saveHighlights {
    NSMutableArray *buf = [NSMutableArray arrayWithCapacity:0];
    
    for ( NSNumber *key in highlights ) {
        [buf addObject:[[highlights objectForKey:key] toDictionary]];
    }
    
    [FileUtil write:[[buf JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding] toFile:
     [NSString stringWithFormat:@"%d/%d.highlight.json" , documentId , [[pageHeads objectAtIndex:currentIndex] intValue]]];
}

- (NSArray *)loadAnnotations {
    NSData *data = [FileUtil read:[NSString stringWithFormat:@"%d/images/%d.anno.json" , documentId , [[pageHeads objectAtIndex:currentIndex] intValue]]];
    return [[NSString stringWithData:data] JSONValue];
}

- (void)loadFreehand {
    NSMutableArray *points = [NSMutableArray arrayWithCapacity:0];
    
    NSData *data = [FileUtil read:[NSString stringWithFormat:@"%d/images/%d.freehand.json" , documentId , [[pageHeads objectAtIndex:currentIndex] intValue]]];
    for ( NSArray *strokeJSON in [[NSString stringWithData:data] JSONValue] ) {
        NSMutableArray *stroke = [NSMutableArray arrayWithCapacity:0];
        for ( NSDictionary *point in strokeJSON ) {
            [stroke addObject:[ObjPoint pointFromDictionary:point]];
        }
        [points addObject:stroke];
    }
    
    [_freehandView performSelectorInBackground:@selector(loadPoints:) withObject:points];
}

- (void)saveFreehand {
    NSMutableArray *buf = [NSMutableArray arrayWithCapacity:0];
    
    for ( NSArray *stroke in _freehandView.points ) {
        NSMutableArray *strokeBuf = [NSMutableArray arrayWithCapacity:0];
        for ( ObjPoint *point in stroke ) {
            [strokeBuf addObject:[point toDictionary]];
        }
        [buf addObject:strokeBuf];
    }
    
    [FileUtil write:[[buf JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding] toFile:
     [NSString stringWithFormat:@"%d/images/%d.freehand.json" , documentId , [[pageHeads objectAtIndex:currentIndex] intValue]]];
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
    [prevTiledScrollView release];
    prevTiledScrollView = nil;
}

#pragma mark TapDetectorDelegate 

- (void)tapDetectorGotSingleTapAtPoint:(CGPoint)tapPoint {
    if ( isIgnoreTap ) {
        isIgnoreTap = NO;
    } else {
        if ( highlightMenuView.alpha > 0 ) {
            [self toggleHighlightMenu];
            [overlayManager clearHighlightSelection];
            currentHighlightSerial = -1;
            
            if ( highlightCommentMenuView.alpha > 0 ) {
                [self toggleHighlightCommentMenu];
            }
        } else {
            [self toggleConfigView];
        }
    }
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

    if ( ![overlayManager selectNearest:tapPoint] ) {
        [[[[UIAlertView alloc] initWithTitle:@"No text found"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
    }
}

#pragma mark TouchObserver

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    if ( !_freehandSwitch.on ) {
        if ( [[[touches anyObject] view] isDescendantOfView:tiledScrollView] ) {
            [tapDetector touchesBegan:touches withEvent:event];
        }
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    if ( !_freehandSwitch.on ) {
        [tapDetector touchesMoved:touches withEvent:event];
        
        UIView *target = [[touches anyObject] view];
        if ( !target || [target isDescendantOfView:tiledScrollView] ) {
            if ( configView.alpha > 0 ) {
                [self toggleConfigView];
            }
            if ( highlightMenuView.alpha > 0 ) {
                [self toggleHighlightMenu];
            }
            if ( highlightCommentMenuView.alpha > 0 ) {
                [self toggleHighlightCommentMenu];
            }
        }
    }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    if ( !_freehandSwitch.on ) {
        if ( [[[touches anyObject] view] isDescendantOfView:tiledScrollView] ) {
            [tapDetector touchesEnded:touches withEvent:event];
        }
    }
}

#pragma mark UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    enum {
        BUTTON_OPEN = 0,
        BUTTON_CANCEL = 1
    };

    switch (buttonIndex) {
        case BUTTON_OPEN:
            switch ( linkMode ) {
                case ImageViewLinkModeURI:
                    if ( ![[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:linkURI]] ) {
                        [[[[UIAlertView alloc] initWithTitle:@"Could not open URL" message:nil delegate:nil
                                           cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
                        break;
                    }
                    
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:linkURI]];

                    break;
                case ImageViewLinkModeGoToPage: {
                    int next = [self calcIndexByPage:linkPage];
                    BOOL isLeft = next > currentIndex ? YES : NO;

                    currentIndex = next;
                    [self movePageToCurrent:isLeft];
                    
                    break;
                }
                default:
                    assert(0);
            }
            break;
        case BUTTON_CANCEL:
            break;
        default:
            assert(0);
    }
    
    [linkURI release];
}

#pragma mark OverlayManagerDelegate

- (void)didBeginSelect {
    if ( configView.alpha > 0 ) {
        [self toggleConfigView];
    }
}

- (void)didEndSelect {
    if ( configView.alpha == 0 ) {
        [self toggleConfigView];
    }
}

- (void)didTouchDownHighlight:(int)serial {
    currentHighlightSerial = serial;
    isIgnoreTap = YES;
    
    if ( configView.alpha > 0 ) {
        [self toggleConfigView];
    }
    if ( highlightMenuView.alpha == 0 ) {
        [self toggleHighlightMenu];
    }
}

- (void)didTouchDownURILink:(NSString *)uri {
    isIgnoreTap = YES;
    
    linkMode = ImageViewLinkModeURI;
    linkURI = [uri retain];
    
    UIActionSheet *sheet = [[[UIActionSheet alloc] initWithTitle:@"May I open this URL?" delegate:self
                                              cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil
                                              otherButtonTitles:@"Open" , nil] autorelease];
    [sheet showInView:self.view];
}

- (void)didTouchDownGoToPageLink:(int)page {
    isIgnoreTap = YES;
    
    linkMode = ImageViewLinkModeGoToPage;
    // change 1-origin to 0-origin
    linkPage = page - 1;
    
    UIActionSheet *sheet = [[[UIActionSheet alloc] initWithTitle:@"May I jump to the page?" delegate:self
                                               cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil
                                               otherButtonTitles:@"Jump" , nil] autorelease];
    [sheet showInView:self.view];
}

#pragma mark UIFreehandViewDelegate

- (void)pointsDidChange:(UIFreehandView *)sender {
    [self saveFreehand];
}

@end
