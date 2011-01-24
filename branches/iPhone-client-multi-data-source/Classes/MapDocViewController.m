//
//  MapDocViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MapDocViewController.h"

#import "ImageViewController.h"
#import "TextViewController.h"
#import "TOCViewController.h"
#import "ThumbnailViewController.h"
#import "BookmarkViewController.h"
#import "BrowserViewController.h"
#import "BookshelfDeletionViewController.h"
#import "DocumentViewerConst.h"
#import "ASIHTTPRequest.h"
#import "Reachability.h"

@interface MapDocViewController ()
- (void)addSubview:(BOOL)animated;
@end

@implementation MapDocViewController

@synthesize datasource = _datasource;


- (void)dealloc {
	[_datasource release];
	[_downloadProgressView release];
	
    [super dealloc];
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
	
	BrowserViewController *c = [BrowserViewController createViewController:_datasource];
	[self pushViewController:c animated:NO];

	// ダウンロードバーの表示
	_downloadProgressView = [[UIProgressView alloc] init];
	CGRect r = CGRectMake(10, self.view.frame.size.height-20, self.view.frame.size.width-20, 9);
	_downloadProgressView.frame = r;
	_downloadProgressView.progress= 0.5;
	_downloadProgressView.alpha = 0;
	[self.view addSubview:_downloadProgressView];
	[self.view bringSubviewToFront:_downloadProgressView];
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



#pragma mark DownloadManagerDelegate

- (void)didMetaInfoDownloadStarted:(id <NSObject>)metaDocumentId
{
	_loading = [[[UIAlertView alloc] initWithTitle:@"\n\nLoading...\nThis process will take few minutes..." message:nil
													  delegate:nil cancelButtonTitle:nil otherButtonTitles:nil] autorelease];
	[_loading show];
}

- (void)didMetaInfoDownloadFinished:(id)docId {
	
    DocumentContext *dc = [[DocumentContext alloc] init];
	dc.documentId = docId;
	
	ImageViewController *ic = [ImageViewController createViewController:_datasource];
	ic.documentContext = dc;
	[self pushViewController:ic animated:YES];
	[dc release];

    [_loading dismissWithClickedButtonIndex:0 animated:YES];
    _loading = nil;
}

- (void)didMetaInfoDownloadFailed:(id)docId error:(NSError*)error
{
	// メタ情報のダウンロード失敗
	NSLog(@"metainfo download failed : %@", docId);
    [_loading dismissWithClickedButtonIndex:0 animated:NO];

	_loading = [[[UIAlertView alloc] initWithTitle:@"\n\nDocument download failed." message:nil
										  delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil] autorelease];
	[_loading show];
}

- (void)didPageDownloadStarted:(id <NSObject>)metaDocumentId
{
	_downloadProgressView.progress = 0;
	_downloadProgressView.alpha = 1.0f;
}

- (void)didPageDownloadFailed:(id <NSObject>)metaDocumentId error:(NSError *)error
{
	_downloadProgressView.alpha = 0.0f;

	// 自動で再開させるので、ユーザへの通知は行わない
}

- (void)pageDownloadProgressed:(id <NSObject>)metaDocumentId downloaded:(float)downloaded
{
	_downloadProgressView.progress = downloaded;
	[_downloadProgressView layoutSubviews];
}

- (void)didAllPagesDownloadFinished:(id <NSObject>)metaDocumentId
{
	_downloadProgressView.alpha = 0.0f;
}

@end
