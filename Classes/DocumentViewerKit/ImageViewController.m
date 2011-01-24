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
#import "DocumentViewerConst.h"
#import "NSString+Data.h"
#import "NSString+Search.h"
#import "RangeObject.h"
#import "ImageSearchViewController.h"
#import "SeparationHolder.h"
#import "HighlightObject.h"
#import "ObjPoint.h"

#import "TOCViewController.h"
#import "ThumbnailViewController.h"
#import "BookmarkViewController.h"
#import "TextViewController.h"

@interface ImageViewController ()

- (TiledScrollView *)buildContentView;
- (void)saveHistory;
- (void)toggleConfigView;
- (void)toggleHighlightMenu;
- (void)toggleHighlightCommentMenu;
- (void)movePageToCurrent:(BOOL)isLeft;
- (UIColor *)highlightColor:(int)index;
- (void)addAnnotations;
- (void)loadHighlights;
- (void)saveHighlights;
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

@synthesize datasource = _datasource;
@synthesize documentContext = _documentContext;

#pragma mark lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];

    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleTapGesture:)];
	[self.view addGestureRecognizer:singleTap];

    UITapGestureRecognizer* doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDoubleTapGesture:)];
	doubleTap.numberOfTapsRequired = 2;
	[self.view addGestureRecognizer:doubleTap];
	[singleTap requireGestureRecognizerToFail:doubleTap];

	[singleTap release];
	[doubleTap release];

	UILongPressGestureRecognizer *rec2 = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleLongPressGesture:)];
	[self.view addGestureRecognizer:rec2];
	[rec2 release];
	
    overlayManager = [OverlayManager new];
    overlayManager.delegate = self;
	overlayManager.datasource = _datasource;
    
    highlights = [[NSMutableDictionary alloc] init];
}


- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];

	[tiledScrollView release];
	tiledScrollView = [[self buildContentView] retain];
	[tiledScrollViewContainer addSubview:tiledScrollView];
}

- (void)viewWillDisappear:(BOOL)animated
{
	// メニューを消す
	if (configView.alpha > 0) {
		[self toggleConfigView];
	}
	[tiledScrollView removeFromSuperview];
	[tiledScrollView release];
	tiledScrollView = nil;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];

}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	[super didRotateFromInterfaceOrientation:fromInterfaceOrientation];

	// willRotateToInterfaceOrientationに移すべき
	[tiledScrollView removeFromSuperview];
	[tiledScrollView release];
	tiledScrollView = [[self buildContentView] retain];
	[tiledScrollViewContainer addSubview:tiledScrollView];
}


- (void)loadDocumentWithDocumentId:(id)docid
{
    [self saveHistory];

	DocumentContext *c = [[DocumentContext alloc] init];
	c.documentId = docid;
	_documentContext = c;
	
	titleLabel.text = [_documentContext title];

	[overlayManager setParam:_documentContext size:tiledScrollView.frame.size];

    [self loadHighlights];
    [self addAnnotations];
    [self loadFreehand];
}


- (void)dealloc {
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
    [prevTiledScrollView release];
    [overlayManager release];
    [highlights release];

	[_documentContext release];
	[_datasource release];
	
    [super dealloc];
}

#pragma mark public

+ (ImageViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource
{
    UIInterfaceOrientation o = [UIDevice currentDevice].orientation;
	ImageViewController *ret = [[[ImageViewController alloc] initWithNibName:
                                 [Util buildNibName:@"Image" orientation:o] bundle:nil] autorelease];
	ret.datasource = datasource;
    return ret;
}

- (IBAction)homeButtonClick:(id)sender {
	//HGMTODO

    [self.navigationController popToRootViewControllerAnimated:YES];
}

- (IBAction)tocViewButtonClick:(id)sender
{
	TOCViewController *tc = [TOCViewController createViewController:_datasource];
	tc.documentContext = _documentContext;
	[self.navigationController pushViewController:tc animated:YES];
}

- (IBAction)thumbnailViewButtonClick:(id)sender
{
	ThumbnailViewController *c = [ThumbnailViewController createViewController:_datasource];
	c.documentContext = _documentContext;
	[self.navigationController pushViewController:c animated:YES];
}

- (IBAction)bookmarkViewButtonClick:(id)sender
{
	BookmarkViewController *c = [BookmarkViewController createViewController:_datasource];
	c.documentContext = _documentContext;
	[self.navigationController pushViewController:c animated:YES];
}

- (IBAction)textViewButtonClick:(id)sender
{
	TextViewController *c = [TextViewController createViewController];
	c.documentContext = _documentContext;
	[self.navigationController pushViewController:c animated:YES];
}

- (IBAction)tweetButtonClick:(id)sender {
    NSURL *url = [NSURL URLWithString:[[NSString stringWithFormat:@"http://twitter.com/home?status=Sample tweet"]
                                       stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    [[UIApplication sharedApplication] openURL:url];
}

- (IBAction)searchButtonClick:(id)sender
{
    BOOL isLand = UIInterfaceOrientationIsLandscape( self.interfaceOrientation );
    
    if ( isLand ) {
        [[[[UIAlertView alloc] initWithTitle:@"Search function is disabled currently on landscape orientation"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
        return;
    }
    
    NSString *orientation = isLand ? @"-land" : @"";
    ImageSearchViewController *searchViewController = [[ImageSearchViewController alloc]
													   initWithNibName:[NSString stringWithFormat:@"ImageSearchViewController%@" , orientation] bundle:nil];
	searchViewController.delegate = self;
    //searchViewController.parent = self;
	searchViewController.documentContext = _documentContext;
 //   searchViewController.docId = _documentContext.documentId;
    
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        popover = [[UIPopoverController alloc] initWithContentViewController:searchViewController];
        popover.popoverContentSize = isLand ? CGSizeMake(480, 320) : CGSizeMake(320, 480);
        [popover presentPopoverFromRect:((UIView *)sender).frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    } else {
		// TODO searchだからmodalの方がよいか?
		[self.navigationController pushViewController:searchViewController animated:YES];
        //[self.view addSubview:searchViewController.view];
    }
	[searchViewController release];
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
	NSString *text = [_datasource imageText:_documentContext.documentId
									   page:_documentContext.currentPage];
    
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

#pragma mark ImageSearchDelegate

- (void)didImageSearchCompleted:(int)page ranges:(NSArray *)ranges selectedIndex:(int)selectedIndex
{
    int next = [_documentContext currentIndexByPage:page];
    if ( next != _documentContext.currentIndex ) {
        BOOL isLeft = next > _documentContext.currentIndex;
        _documentContext.currentIndex = next;
        [self movePageToCurrent:isLeft];
    }

    [overlayManager showSearchResult:ranges selectedIndex:selectedIndex];

    [self toggleConfigView];

    [self didImageSearchCanceled];
}

- (void)didImageSearchCanceled
{
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        [popover dismissPopoverAnimated:YES];
        [popover release];
        popover = nil;
    } else {
		[self.navigationController popViewControllerAnimated:YES];
        //[searchViewController.view removeFromSuperview];
    }
}

#pragma mark private

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
//    tapDetector.basisView = view.zoomableContainerView;
    overlayManager.scrollView = view;
    overlayManager.markerView = markerView;
    overlayManager.balloonContainerView = balloonContainerView;
}

- (TiledScrollView *)buildContentView {
	return [self buildContentViewWithInterfaceOrientation:self.interfaceOrientation];
}

- (TiledScrollView *)buildContentViewWithInterfaceOrientation:(UIInterfaceOrientation)o {
    CGRect frame = tiledScrollViewContainer.bounds;
    float w = frame.size.width;
    float h = frame.size.height;
    float baseScale = 1.0f;
	if ( UIInterfaceOrientationIsLandscape(o) ) {
        w /= 2;
        baseScale = 0.5;
        
		if ([_documentContext isSinglePage]) {
//        if ( [[isSinglePage objectAtIndex:currentIndex] boolValue] ) {
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
    [_datasource saveHistory:_documentContext];
}

- (void)saveHighlights {
	NSMutableArray *buf = [NSMutableArray arrayWithCapacity:0];
 
	for ( NSNumber *key in highlights ) {
		[buf addObject:[[highlights objectForKey:key] toDictionary]];
	}

	// TODO
	[_datasource saveHighlights:_documentContext.documentId
						   page:_documentContext.currentPage
						   data:buf];
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

	[_datasource saveFreehand:_documentContext.documentId
						 page:_documentContext.currentPage
						 data:buf];
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

	NSArray *a = [_documentContext annotations];
	for ( NSDictionary *dict in a ) {
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
/*
	titleLabel.text = [_datasource toc:_documentContext.documentId
								  page:_documentContext.currentPage].text;
*/
	titleLabel.text = [_documentContext title];
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
    
	[overlayManager setParam:_documentContext size:tiledScrollView.frame.size];

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

- (void)loadHighlights {
	NSArray *a = [_documentContext highlights];
  	for ( NSDictionary *dic in a) { 
        HighlightObject *highlight = [HighlightObject objectWithDictionary:dic];

        int serial = [overlayManager showHighlight:[highlight range] color:[self highlightColor:highlight.color] selecting:NO];
        [overlayManager changeHighlightComment:serial text:highlight.text];

        [highlights setObject:highlight forKey:[NSNumber numberWithInt:serial]];
    }
}


- (void)loadFreehand
{
    NSMutableArray *points = [NSMutableArray arrayWithCapacity:0];

    NSArray *a = [_documentContext freehand];
    for ( NSArray *strokeJSON in a) {
        NSMutableArray *stroke = [NSMutableArray arrayWithCapacity:0];
        for ( NSDictionary *point in strokeJSON ) {
            [stroke addObject:[ObjPoint pointFromDictionary:point]];
        }
        [points addObject:stroke];
    }
    [_freehandView performSelectorInBackground:@selector(loadPoints:) withObject:points];
}

#pragma mark TiledScrollViewDataSource method

- (UIView *)tiledScrollViewGetTile:(TiledScrollView *)tiledScrollView row:(int)row column:(int)column resolution:(int)resolution
{
	resolution += [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
	// HGMTODO
	//	if ([pageHeads count] < 1) return nil;
	
    int page = _documentContext.currentPage;
	BOOL isLandscape = UIInterfaceOrientationIsLandscape(self.interfaceOrientation);
    if ( isLandscape && ![_documentContext isSinglePage]) {
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
	return [_documentContext getTileImageWithType:type page:page column:column row:row resolution:resolution];
}



- (void)tiledScrollViewDidSlide:(TiledScrollViewSlideDirection)direction {

    BOOL isLeft = (direction == TiledScrollViewSlideDirectionLeft);
    int delta = isLeft ? 1 : -1;
    
	if (![_documentContext isValidIndex:_documentContext.currentIndex+delta]) return;
	
	_documentContext.currentIndex += delta;
//    currentIndex += delta;
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

#pragma mark UIGestureRecognizer handler

- (void)handleSingleTapGesture:(UIGestureRecognizer *)gestureRecognizer
{
	NSLog(@"single");
	CGPoint p = [gestureRecognizer locationInView:gestureRecognizer.view];

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

- (void)handleDoubleTapGesture:(UIGestureRecognizer *)gestureRecognizer
{
	NSLog(@"double");
	CGPoint p = [gestureRecognizer locationInView:gestureRecognizer.view];

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
    tiledScrollView.contentOffset = CGPointMake(MAX(MIN(p.x * toScale - boundsSize.width / 2 ,
														boundsSize.width * (toScale - 1)) , 0),
												MAX(MIN(p.y * toScale - boundsSize.height / 2 ,
														boundsSize.height * (toScale - 1)) , 0));
    [overlayManager applyScaleView:toScale];
	
    [UIView commitAnimations];
}

- (void)handleSingleLongPressGesture:(UIGestureRecognizer *)gestureRecognizer
{
	NSLog(@"long");
	CGPoint p = [gestureRecognizer locationInView:gestureRecognizer.view];

	BOOL isLand = UIInterfaceOrientationIsLandscape( self.interfaceOrientation );
    
    if ( isLand ) {
        [[[[UIAlertView alloc] initWithTitle:@"Selecting function is disabled currently on landscape orientation"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
        return;
    }
	
    if ( ![overlayManager selectNearest:p] ) {
        [[[[UIAlertView alloc] initWithTitle:@"No text found"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
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
                    int next = [_documentContext currentIndexByPage:linkPage];
                    BOOL isLeft = next > _documentContext.currentIndex ? YES : NO;

                    _documentContext.currentIndex = next;
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
