//
//  BrowserViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/16.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IUIViewController.h"

@interface BrowserViewController : IUIViewController {
    UIWebView *webView;
}

@property(nonatomic,retain) IBOutlet UIWebView *webView;

+ (BrowserViewController *)createViewController;

@end
