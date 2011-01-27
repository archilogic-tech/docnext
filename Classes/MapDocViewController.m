//
//  MapDocViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MapDocViewController.h"
#import "ImageViewController.h"
#import "BrowserViewController.h"


@implementation MapDocViewController

@synthesize datasource = _datasource;


- (void)dealloc {
	[_datasource release];
	[_downloadProgressView release];
	[_loading release];
	
    [super dealloc];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
	self.navigationBarHidden = YES;
	
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


#pragma mark DownloadManagerDelegate

- (void)didMetaInfoDownloadStarted:(id <NSObject>)metaDocumentId
{
	if (_loading) return;
	_loading = [[[UIAlertView alloc] initWithTitle:@"\n\nLoading...\nThis process will take few minutes..." message:nil
													  delegate:nil cancelButtonTitle:nil otherButtonTitles:nil] autorelease];
	[_loading show];
}

- (void)didMetaInfoDownloadFinished:(id)docId {
	
    DocumentContext *dc = [[DocumentContext alloc] init];
	dc.documentId = docId;
	
	ImageViewController *ic = [ImageViewController createViewController:_datasource];
	ic.documentContext = dc;

	// カスタマイズしたConfigControllerを設定できる
	//ic.configViewController = ...
	
	[ic showInViewController:self];
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
										  delegate:self cancelButtonTitle:nil otherButtonTitles:@"OK", nil] autorelease];
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
	/*
	 if ( [delegate respondsToSelector:@selector(didPageDownloadFailed:error:)] ) {
	 [delegate didPageDownloadFailed:docId error:[request error]];
	 }
	 NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
	 */
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

#pragma mark UIAlertViewDelegate

- (void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	_loading = nil;
}


@end
