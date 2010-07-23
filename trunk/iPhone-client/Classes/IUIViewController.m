    //
//  IUIViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/06.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "IUIViewController.h"

@implementation IUIViewController

@synthesize progressView;
@synthesize parent;

+ (NSString *)buildNibName:(NSString *)prefix {
    NSString *target = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"-iPad" : @"-iPhone";
    NSString *orientation = UIDeviceOrientationIsLandscape( [UIDevice currentDevice].orientation ) ? @"-land" : @"";
    return [NSString stringWithFormat:@"%@ViewController%@%@" , prefix , target , orientation];
}

- (IUIViewController *)createViewController {
    [NSException raise:NSInternalInconsistencyException format:@"Sholud override IUIViewController.viewControllerWithOrientation"];
    return nil;
}

- (void)setLandspace {
    isLandscape = UIDeviceOrientationIsLandscape( [UIDevice currentDevice].orientation );
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ( ![[DownloadManager instance] hasDownloading] ) {
        [self.progressView removeFromSuperview];
    }
}

- (void)dealloc {
    [progressView release];
    
    [super dealloc];
}

#pragma mark DownloadManagerDelegate

- (void)downloadProgressed:(int)docId downloaded:(float)downloaded {
    self.progressView.progress = downloaded;
}

- (void)downloadCompleted:(int)docId {
    [self.progressView removeFromSuperview];
}

@end
