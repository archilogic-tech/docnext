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
#import "Const.h"
#import "ASIHTTPRequest.h"
#import "FileUtil.h"
#import "ZipArchive.h"
#import "DownloadManager.h"
#import "Reachability.h"

@interface MapDocViewController ()
- (void)addSubview:(BOOL)animated;
@end

@implementation MapDocViewController

@synthesize current;
@synthesize window;

- (void)showBrowser:(BOOL)animated {
    [self.current.view removeFromSuperview];
    
    self.current = [BrowserViewController createViewController:self.interfaceOrientation];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:animated];
}

- (void)showBookshelf:(BOOL)animated {
    [self.current.view removeFromSuperview];
    
    self.current = [BookshelfViewController createViewController:self.interfaceOrientation];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:animated];
}

- (void)showHome:(BOOL)animated {
    if ( [[Reachability reachabilityWithHostName:ReachabilityHost] currentReachabilityStatus] != NotReachable ) {
        [self showBrowser:animated];
    } else {
        [self showBookshelf:animated];
    }
}

- (void)showBookshelfDeletion {
    [self.current.view removeFromSuperview];
    
    self.current = [BookshelfDeletionViewController createViewController:self.interfaceOrientation];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:YES];
}

- (void)showImage:(int)documentId page:(int)page {
    [self.current.view removeFromSuperview];
    
    self.current = [ImageViewController createViewController:self.interfaceOrientation docId:documentId page:page window:window];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:YES];
}

- (void)showTOC:(int)documentId prevPage:(int)prevPage {
    [self.current.view removeFromSuperview];
    
    self.current = [TOCViewController createViewController:self.interfaceOrientation docId:documentId prevPage:prevPage];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:YES];
}

- (void)showThumbnail:(int)documentId page:(int)page {
    [self.current.view removeFromSuperview];
    
    self.current = [ThumbnailViewController createViewController:self.interfaceOrientation docId:documentId page:page];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:YES];
}

- (void)showBookmark:(int)documentId page:(int)page {
    [self.current.view removeFromSuperview];
    
    self.current = [BookmarkViewController createViewController:self.interfaceOrientation docId:documentId page:page
                                                          title:[FileUtil toc:documentId page:page].text];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
    [self addSubview:YES];
}

- (void)showText:(int)documentId page:(int)page {
    [self.current.view removeFromSuperview];
    
    self.current = [TextViewController createViewController:self.interfaceOrientation docId:documentId page:page];
    self.current.parent = self;
    [DownloadManager instance].delegate = self.current;
    
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
    [DownloadManager instance].delegate = self.current;
    
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
    
    [self showHome:NO];
}

- (void)dealloc {
    [current release];

    [super dealloc];
}

@end
