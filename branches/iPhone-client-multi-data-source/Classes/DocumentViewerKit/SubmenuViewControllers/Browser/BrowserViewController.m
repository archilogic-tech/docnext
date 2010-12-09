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

+ (BrowserViewController *)createViewController:(UIInterfaceOrientation)orientation
									 datasource:(id<NSObject,DocumentViewerDatasource>)datasource
{
    BrowserViewController *ret = [[[BrowserViewController alloc] initWithNibName:
                                   [IUIViewController buildNibName:@"Browser" orientation:orientation] bundle:nil] autorelease];
    [ret setLandspace:orientation];
	ret.datasource = datasource;
    return ret;
}

- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation {
    return [BrowserViewController createViewController:orientation datasource:_datasource];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
	NSString *libraryURL = [_datasource libraryURL];
    [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:libraryURL]]];
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
