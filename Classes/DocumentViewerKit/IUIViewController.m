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

+ (NSString *)buildNibName:(NSString *)prefix orientation:(UIInterfaceOrientation)orientation {
    NSString *target = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"-iPad" : @"-iPhone";
    NSString *orient = UIInterfaceOrientationIsLandscape( orientation ) ? @"-land" : @"";
    return [NSString stringWithFormat:@"%@ViewController%@%@" , prefix , target , orient];
}

- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation {
    [NSException raise:NSInternalInconsistencyException format:@"Sholud override IUIViewController.viewControllerWithOrientation"];
    return nil;
}

- (void)setLandspace:(UIInterfaceOrientation)orientation {
    isLandscape = UIInterfaceOrientationIsLandscape( orientation );
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
	[self.progressView removeFromSuperview];
/*
    if ( ![[DownloadManager instance] hasDownloading] ) {
        [self.progressView removeFromSuperview];
    }
*/
 }

- (void)dealloc {
    [progressView release];
    
    [super dealloc];
}

#pragma mark DownloadManagerDelegate

- (void)pageDownloadProgressed:(id)docId downloaded:(float)downloaded {
    self.progressView.progress = downloaded;
}

- (void)didAllPagesDownloadFinished:(id)docId {
    [self.progressView removeFromSuperview];
}

@end
