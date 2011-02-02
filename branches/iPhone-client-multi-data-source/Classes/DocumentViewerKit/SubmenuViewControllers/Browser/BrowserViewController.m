//
//  BrowserViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/16.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BrowserViewController.h"
#import "DocumentViewerConst.h"

@implementation BrowserViewController

@synthesize webView;
@synthesize datasource = _datasource;
@synthesize libraryUrl = _libraryUrl;

+ (BrowserViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource
{
	UIInterfaceOrientation o = [UIDevice currentDevice].orientation;
    
	BrowserViewController *ret = [[[BrowserViewController alloc] initWithNibName:
                                   [Util buildNibName:@"Browser" orientation:o] bundle:nil] autorelease];
	ret.datasource = datasource;
    return ret;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:_libraryUrl]]];
}

- (void)dealloc {
    [webView release];
    
    [super dealloc];
}

- (BOOL) webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
	if ([[[request URL] scheme] isEqualToString:@"mapdoc"]) {
		[[UIApplication sharedApplication]  openURL:[request URL]];
		return NO;
	}
	
	NSLog(@"REQ : %@", request);
	return YES;
}


- (void) webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
	NSLog(@"ERR : %@", error);
}

- (void) webViewDidFinishLoad:(UIWebView *)webView
{
	NSLog(@"Finish : %@", webView);
}


@end
