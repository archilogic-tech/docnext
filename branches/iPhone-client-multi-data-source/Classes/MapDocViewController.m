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

@interface MapDocViewController ()
- (void)addSubview:(BOOL)animated;
@end

@implementation MapDocViewController

@synthesize window;
@synthesize datasource = _datasource;

- (void)showBrowser:(BOOL)animated {
	BrowserViewController *c = [BrowserViewController createViewController:_datasource];
	[self pushViewController:c animated:animated];
}

- (void)showBookshelf:(BOOL)animated {

    BookshelfViewController *c = [BookshelfViewController createViewController:_datasource];
    
//	[self.current.view removeFromSuperview];
//    self.current = c;
    [self addSubview:animated];
}

#define UstDocReachabilityHost (@"ustdoc.com")

- (void)showHome:(BOOL)animated {
	[self popToRootViewControllerAnimated:YES];
/*	
	if ( [[Reachability reachabilityWithHostName:UstDocReachabilityHost] currentReachabilityStatus] != NotReachable ) {
//	if (NO) {
        [self showBrowser:animated];
    } else {
        [self showBookshelf:animated];
    }
 */
}

- (void)showBookshelfDeletion {
    
	BookshelfDeletionViewController *c = [BookshelfDeletionViewController createViewController:_datasource];
    
//	[self.current.view removeFromSuperview];
//    self.current = c;
    [self addSubview:YES];
}

- (void)showImage:(DocumentContext*)documentContext {

	ImageViewController *ic = [ImageViewController createViewController:_datasource];
	ic.documentContext = documentContext;
	[self pushViewController:ic animated:YES];
}


- (void)addSubview:(BOOL)animated {
    if ( animated ) {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:1];
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView setAnimationTransition:UIViewAnimationTransitionFlipFromLeft forView:self.view cache:YES];
    }
	
//	[self.view addSubview:self.current.view];
    
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
	
//    self.current.view.alpha = 0;
    
	[UIView commitAnimations];
}

- (void)fadeOutAnimationDidStop:(NSString *)animationId finished:(NSNumber *)finished context:(void *)context {
//    [self.current.view removeFromSuperview];
    
//    self.current = [self.current createViewController:willInterfaceOrientation];
//    self.current.parent = self;

	/*
	_datasource.downloadManagerDelegate = self.current;
    
    [self.view addSubview:self.current.view];
    self.current.view.alpha = 0;

    [UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	[UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self.view cache:YES];
	
    self.current.view.alpha = 1;
    
	[UIView commitAnimations];
	 */
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
	[super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    willInterfaceOrientation = toInterfaceOrientation;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
	[super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
	//    [self addSubviewFade];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
	self.navigationBarHidden = YES;
	
	// HGMTODO
	// debug
	//NSString *url = @"mapdoc://test.md-dc.jp/book/dl/exec/0000005x/0000002a/docnext/7ocw89xfgxf9y4b1/?p=000ghnpc&v=00001bte";
	//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];

	// ライブラリviewを表示する
	//[self showHome:NO];

	[self showBrowser:NO];

}

- (void)dealloc {
//    [current release];
	[_datasource release];
	
    [super dealloc];
}

@end
