//
//  ImageViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "ImageViewController.h"
#import "DocumentViewerConst.h"

#import "HighlightObject.h"
#import "ObjPoint.h"


@interface ImageViewController ()

- (TiledScrollView *)buildContentView;
- (void)saveHistory;
- (void)movePageToCurrent:(PageTransitionAnimationType)isLeft;
- (UIColor *)highlightColor:(int)index;
- (void)addAnnotations;
- (void)loadHighlights;
- (void)saveHighlights;
- (void)saveFreehand;
- (void)loadFreehand;

@end

@implementation ImageViewController

@synthesize freehandView = _freehandView;
@synthesize configViewController = _configViewController;
@synthesize tiledScrollViewContainer = _tiledScrollViewContainer;
@synthesize datasource = _datasource;
@synthesize documentContext = _documentContext;


@synthesize selectionMenuView;
@synthesize highlightMenuView;
@synthesize highlightCommentMenuView;

@synthesize highlightCommentTextField;

@synthesize overlayManager;


#pragma mark lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];

    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleTapGesture:)];
	[_tiledScrollViewContainer addGestureRecognizer:singleTap];

    UITapGestureRecognizer* doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDoubleTapGesture:)];
	doubleTap.numberOfTapsRequired = 2;
	[_tiledScrollViewContainer addGestureRecognizer:doubleTap];
	[singleTap requireGestureRecognizerToFail:doubleTap];

	[doubleTap release];

	UILongPressGestureRecognizer *rec2 = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleLongPressGesture:)];
	[_tiledScrollViewContainer addGestureRecognizer:rec2];
	[rec2 requireGestureRecognizerToFail:singleTap];
	[rec2 release];

	[singleTap release];

    overlayManager = [OverlayManager new];
    overlayManager.delegate = self;
	overlayManager.datasource = _datasource;
    
    highlights = [[NSMutableDictionary alloc] init];

	[tiledScrollView removeFromSuperview];
	[tiledScrollView release];
	tiledScrollView = [[self buildContentView] retain];
	[_tiledScrollViewContainer addSubview:tiledScrollView];

	_configViewController = [[ConfigViewController alloc] initWithNibName:@"ConfigView" bundle:nil];
	_configViewController.parent = self;
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	[self movePageToCurrent:PageTransitionNone];
}

- (void)viewWillDisappear:(BOOL)animated
{
	// メニューを消す
	[self setHideConfigView:YES];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];

	// 確実に回転させる
	[_documentContext didInterfaceOrientationChanged:self.interfaceOrientation];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	[super didRotateFromInterfaceOrientation:fromInterfaceOrientation];

	[self movePageToCurrent:PageTransitionNone];
}

- (void)setDocumentContext:(DocumentContext *)dc
{
    [self saveHistory];
	
	[_documentContext release];
	_documentContext = [dc retain];

	[self movePageToCurrent:PageTransitionNone];
}

- (void)dealloc {
    [_configViewController release];

    [tiledScrollView release];
    [_tiledScrollViewContainer release];
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


- (IBAction)copyButtonClick {
	NSString *text = [_documentContext imageText];
	[[UIPasteboard generalPasteboard] setString:[text substringWithRange:[overlayManager selection]]];

    [self setHideConfigView:YES];
    [overlayManager clearSelection];

	isIgnoreTap = YES;
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
    
	[self setHideConfigView:YES];
    [overlayManager clearSelection];
    
	[self setHideHighlightMenu:NO];
}

- (IBAction)highlightCommentButtonClick {
    HighlightObject *highlight = [highlights objectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    highlightCommentTextField.text = highlight.text;
    
	[self setHideHighlightCommentMenu:NO];
}

- (IBAction)highlightCommentApplyButtonClick {
    [highlightCommentTextField resignFirstResponder];
    
    [overlayManager changeHighlightComment:currentHighlightSerial text:highlightCommentTextField.text];
    [overlayManager applyScaleView:tiledScrollView.zoomScale];
    
    HighlightObject *highlight = [highlights objectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    highlight.text = highlightCommentTextField.text;
    [self saveHighlights];

	[self setHideHighlightCommentMenu:YES];
}

- (IBAction)highlightChangeColorClick:(UIButton *)sender {
    [overlayManager changeHighlightColor:currentHighlightSerial color:[self highlightColor:sender.tag]];

    HighlightObject *highlight = [highlights objectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    highlight.color = sender.tag;
    [self saveHighlights];
    
	[self setHideHighlightMenu:YES];
	[self setHideHighlightCommentMenu:YES];
}

- (IBAction)highlightDeleteClick {
    [overlayManager deleteHighlight:currentHighlightSerial];

    [highlights removeObjectForKey:[NSNumber numberWithInt:currentHighlightSerial]];
    [self saveHighlights];
    
    currentHighlightSerial = -1;

	[self setHideHighlightMenu:YES];
	[self setHideHighlightCommentMenu:YES];
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
    overlayManager.scrollView = view;
    overlayManager.markerView = markerView;
    overlayManager.balloonContainerView = balloonContainerView;
}

- (TiledScrollView *)buildContentView {
	return [self buildContentViewWithInterfaceOrientation:self.interfaceOrientation];
}

- (TiledScrollView *)buildContentViewWithInterfaceOrientation:(UIInterfaceOrientation)o {
    CGRect frame = _tiledScrollViewContainer.bounds;
    float w = frame.size.width;
    float h = frame.size.height;
    float baseScale = 1.0f;
	if ( UIInterfaceOrientationIsLandscape(o) ) {
        w /= 2;
        baseScale = 0.5;
        
		if ([_documentContext isSingleIndex]) {
			// centering
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

	// TODO ZZZ
	[_datasource saveHighlights:_documentContext.documentId
						   page:_documentContext.currentPage
						   data:buf];
}


- (BOOL)hideConfigView
{
	return (_configViewController.view.alpha == 0);
}

- (void)setHideConfigView:(BOOL)b
{
    double dst = b ? 0 : 1;
    
	if (b) [_configViewController viewWillDisappear:YES];
	else [_configViewController viewWillAppear:NO];
	
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];

    // TODO
	//[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
    _configViewController.view.alpha = dst;
    if ( [overlayManager hasSelection] ) {
		[self setHideSelectionMenuView:b];
    }
	[self setHideHighlightMenu:YES];
    [UIView commitAnimations];

	if (b) [_configViewController viewDidDisappear:YES];
	else [_configViewController viewDidAppear:YES];


}

- (BOOL)hideSelectionMenu
{
	return (selectionMenuView.alpha == 0);
}

- (void)setHideSelectionMenuView:(BOOL)b
{
    double dst = b ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	// TODO
	//    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
    selectionMenuView.alpha = dst;
    
    [UIView commitAnimations];
}


- (BOOL)hideHighlightMenu
{
	return (highlightMenuView.alpha == 0);
}

- (void)setHideHighlightMenu:(BOOL)b
{
    double dst = b ? 0 : 1;
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
//    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
    highlightMenuView.alpha = dst;
    
    [UIView commitAnimations];
}

- (BOOL)hideHighlightCommentMenu
{
	return (highlightCommentMenuView.alpha == 0);
}


- (void)setHideHighlightCommentMenu:(BOOL)b
{
    double dst = b ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];

	// TODO
	// [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:configView cache:NO];
    
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



- (void)movePageToCurrent:(PageTransitionAnimationType)animationType {


    if (animationType != PageTransitionNone) {
		prevTiledScrollView = tiledScrollView;
		[prevTiledScrollView removeFromSuperview];
		
		tiledScrollView = [[self buildContentView] retain];
		[_tiledScrollViewContainer addSubview:tiledScrollView];

		CATransition *animation = [CATransition animation];
		animation.duration = 0.5;
		animation.type = kCATransitionPush;
		animation.subtype = (animationType == PageTransitionFromLeft) ? kCATransitionFromLeft : kCATransitionFromRight;
		animation.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
		animation.delegate = self;
		[self.view.layer addAnimation:animation forKey:nil];
	} else {
		[tiledScrollView removeFromSuperview];
		[tiledScrollView release];
		tiledScrollView = [[self buildContentView] retain];
		[_tiledScrollViewContainer addSubview:tiledScrollView];
	}

	[overlayManager clearSelection];
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
    
    int page = _documentContext.currentPage;
	BOOL isLandscape = UIInterfaceOrientationIsLandscape(self.interfaceOrientation);
    if ( isLandscape && ![_documentContext isSingleIndex]) {
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
	[self movePageToCurrent:isLeft ? PageTransitionFromLeft : PageTransitionFromRight];
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

	// コメント入力メニューが表示されているときはなにもしない
	if (![self hideHighlightCommentMenu]) return;
	
	CGPoint p = [gestureRecognizer locationInView:gestureRecognizer.view];

    if ( isIgnoreTap ) {
        isIgnoreTap = NO;
    } else {
		
		if (![self hideHighlightMenu]) {
            [self setHideHighlightMenu:YES];
            [overlayManager clearHighlightSelection];
            currentHighlightSerial = -1;
            
			[self setHideHighlightCommentMenu:YES];
        } else {
			[self setHideConfigView:![self hideConfigView]];
        }
    }
}

- (void)handleDoubleTapGesture:(UIGestureRecognizer *)gestureRecognizer
{
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

	} else {
		isIgnoreTap = YES;
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
                    [self movePageToCurrent:isLeft ? PageTransitionFromLeft : PageTransitionFromRight];
                    
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
	[self setHideConfigView:YES];
}

- (void)didEndSelect {
	[self setHideConfigView:NO];
}

- (void)didTouchDownHighlight:(int)serial {
    currentHighlightSerial = serial;
    isIgnoreTap = YES;
    
	[self setHideConfigView:YES];
	[self setHideHighlightMenu:NO];
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
