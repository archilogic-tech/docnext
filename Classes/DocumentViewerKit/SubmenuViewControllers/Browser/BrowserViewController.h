//
//  BrowserViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/16.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentViewerDatasource.h"

#define LibraryURL (@"http://ustdoc.com/docman_optimage/library.html")

@interface BrowserViewController : UIViewController<UIWebViewDelegate> {
	UIWebView *webView;
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet UIWebView *webView;
@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;


+ (BrowserViewController *)createViewController:(id<DocumentViewerDatasource>)datasource;

@end
