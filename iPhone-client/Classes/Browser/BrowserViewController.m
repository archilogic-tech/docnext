//
//  BrowserViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/16.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BrowserViewController.h"
#import "Const.h"

@implementation BrowserViewController

@synthesize webView;

+ (BrowserViewController *)createViewController {
    BrowserViewController *ret = [[[BrowserViewController alloc] initWithNibName:[IUIViewController buildNibName:@"Browser"] bundle:nil] autorelease];
    [ret setLandspace];
    return ret;
}

- (IUIViewController *)createViewController {
    return [BrowserViewController createViewController];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:LibraryURL]]];
}

- (void)dealloc {
    [webView release];
    
    [super dealloc];
}


@end
