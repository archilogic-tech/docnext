//
//  IUIViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/06.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DownloadManagerDelegate.h"

@class MapDocViewController;

@interface IUIViewController : UIViewController<DownloadManagerDelegate> {
    UIProgressView *progressView;
    
    MapDocViewController *parent;
    BOOL isLandscape;
}

@property(nonatomic,retain) IBOutlet UIProgressView *progressView;
@property(nonatomic,assign) MapDocViewController *parent;

+ (NSString *)buildNibName:(NSString *)prefix orientation:(UIInterfaceOrientation)orientation;
- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation;
- (void)setLandspace:(UIInterfaceOrientation)orientation;

@end
