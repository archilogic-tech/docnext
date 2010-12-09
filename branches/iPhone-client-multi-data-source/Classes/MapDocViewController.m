//
//  MapDocViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "ImageViewController.h"
#import "TextViewController.h"
#import "TOCViewController.h"
#import "ThumbnailViewController.h"
#import "BookmarkViewController.h"
#import "BrowserViewController.h"
#import "BookshelfViewController.h"
#import "BookshelfDeletionViewController.h"
#import "DocumentViewerConst.h"
#import "ASIHTTPRequest.h"
#import "Reachability.h"
#import "SampleConst.h"

#import "SampleDatasource.h"

@interface MapDocViewController ()
- (void)addSubview:(BOOL)animated;
@end

@implementation MapDocViewController

@synthesize current;
@synthesize window;
@synthesize datasource = _datasource;

- (void)showBrowser:(BOOL)animated {

	BrowserViewController *c = [BrowserViewController createViewController:self.interfaceOrientation datasource:_datasource];
	c.parent = self;
	((SampleDatasource*)_datasource).downloadManagerDelegate = c;
	
	[self.current.view removeFromSuperview];
    self.current = c;
    [self addSubview:animated];
}

- (void)showBookshelf:(BOOL)animated {

    BookshelfViewController *c = [BookshelfViewController createViewController:self.interfaceOrientation datasource:_datasource];
    c.parent = self;
	((SampleDatasource*)_datasource).downloadManagerDelegate = c;
    
	[self.current.view removeFromSuperview];
    self.current = c;
    [self addSubview:animated];
}

- (void)showHome:(BOOL)animated {
    if ( [[Reachability reachabilityWithHostName:MediaDoReachabilityHost] currentReachabilityStatus] != NotReachable ) {
//	if (NO) {
        [self showBrowser:animated];
    } else {
        [self showBookshelf:animated];
    }
}

- (void)showBookshelfDeletion {
    
	BookshelfDeletionViewController *c = [BookshelfDeletionViewController createViewController:self.interfaceOrientation datasource:_datasource];
    c.parent = self;
	((SampleDatasource*)_datasource).downloadManagerDelegate = c;
    
	[self.current.view removeFromSuperview];
    self.current = c;
    [self addSubview:YES];
}

- (void)showImage:(id)documentId page:(int)page {

	ImageViewController *ic = [ImageViewController createViewController:self.interfaceOrientation
															 datasource:_datasource
																 window:window];
	ic.parent = self;
    ic.documentId = documentId;
	[ic setIndexByPage:page];
	((SampleDatasource*)_datasource).downloadManagerDelegate = ic;
    
	[self.current.view removeFromSuperview];
    self.current = ic;
    [self addSubview:YES];
}

- (void)showTOC:(id)documentId prevPage:(int)prevPage {
    
	TOCViewController *tc = [TOCViewController createViewController:self.interfaceOrientation
														 datasource:_datasource];
	
	
	tc.parent = self;
	tc.documentId = documentId;
	tc.prevPage = prevPage;
	((SampleDatasource*)_datasource).downloadManagerDelegate = tc;

    [self.current.view removeFromSuperview];
    self.current = tc;
    [self addSubview:YES];
}

- (void)showThumbnail:(id)documentId page:(int)page {
    
	ThumbnailViewController *c = [ThumbnailViewController createViewController:self.interfaceOrientation datasource:_datasource];
	c.parent = self;
	c.documentId = documentId;
	c.page = page;
	((SampleDatasource*)_datasource).downloadManagerDelegate = c;

	[self.current.view removeFromSuperview];
	self.current = c;
    [self addSubview:YES];
}

- (void)showBookmark:(id)documentId page:(int)page {
    
	NSString *title = [_datasource toc:documentId page:page].text;

    BookmarkViewController *c = [BookmarkViewController createViewController:self.interfaceOrientation datasource:_datasource];
	c.parent = self;
	c.currentDocumentId = documentId;
	c.currentPage = page;
	c.currentTitle = title;
	((SampleDatasource*)_datasource).downloadManagerDelegate = c;

	[self.current.view removeFromSuperview];
	self.current = c;
    [self addSubview:YES];
}

- (void)showText:(id)documentId page:(int)page {
    
	TextViewController *c = [TextViewController createViewController:self.interfaceOrientation datasource:_datasource];
	c.parent = self;
	c.documentId = documentId;
	c.currentPage = page;
	((SampleDatasource*)_datasource).downloadManagerDelegate = c;

	[self.current.view removeFromSuperview];
	self.current = c;
    [self addSubview:YES];
}

- (void)addSubview:(BOOL)animated {
    if ( animated ) {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:1];
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView setAnimationTransition:UIViewAnimationTransitionFlipFromLeft forView:self.view cache:YES];
    }
	
	[self.view addSubview:self.current.view];
    
    if ( animated ) {
        [UIView commitAnimations];
    }
}

- (void)addSubviewFade {
    [UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self.view cache:YES];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDidStopSelector:@selector(fadeOutAnimationDidStop:finished:context:)];
	
    self.current.view.alpha = 0;
    
	[UIView commitAnimations];
}

- (void)fadeOutAnimationDidStop:(NSString *)animationId finished:(NSNumber *)finished context:(void *)context {
    [self.current.view removeFromSuperview];
    
    self.current = [self.current createViewController:willInterfaceOrientation];
    self.current.parent = self;
	((SampleDatasource*)_datasource).downloadManagerDelegate = self.current;
    
    [self.view addSubview:self.current.view];
    self.current.view.alpha = 0;

    [UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self.view cache:YES];
	
    self.current.view.alpha = 1;
    
	[UIView commitAnimations];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    willInterfaceOrientation = toInterfaceOrientation;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
    [self addSubviewFade];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
	// HGMTODO
	// debug
	//NSString *url = @"mapdoc://test.md-dc.jp/book/dl/exec/0000005x/0000002a/docnext/7ocw89xfgxf9y4b1/?p=000ghnpc&v=00001bte";
	//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];

	// ライブラリviewを表示する
	[self showHome:NO];
}

- (void)dealloc {
    [current release];
	[_datasource release];
	
    [super dealloc];
}

@end
