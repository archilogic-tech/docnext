//
//  BrowserViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/16.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentViewerDatasource.h"

@interface BrowserViewController : UIViewController<UIWebViewDelegate> {
	UIWebView *webView;
	id<NSObject,DocumentViewerDatasource> _datasource;
	NSString *_libraryUrl;
}

@property(nonatomic,retain) IBOutlet UIWebView *webView;
@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;
@property(nonatomic, copy) NSString* libraryUrl;


+ (BrowserViewController *)createViewController:(id<DocumentViewerDatasource>)datasource;

@end
