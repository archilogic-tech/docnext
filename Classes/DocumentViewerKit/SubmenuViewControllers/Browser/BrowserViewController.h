//
//  BrowserViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/16.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IUIViewController.h"
#import "DocumentViewerDatasource.h"

@interface BrowserViewController : IUIViewController<UIWebViewDelegate> {
    UIWebView *webView;
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet UIWebView *webView;
@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;


+ (BrowserViewController *)createViewController:(UIInterfaceOrientation)orientation datasource:(id<DocumentViewerDatasource>)datasource;

@end
