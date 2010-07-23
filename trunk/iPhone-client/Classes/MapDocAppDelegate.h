//
//  MapDocAppDelegate.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DownloadManager.h"

@class MapDocViewController;

@interface MapDocAppDelegate : NSObject <UIApplicationDelegate , DownloadManagerDelegate> {
    UIWindow *window;
    MapDocViewController *viewController;
    
    UIAlertView *loading;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet MapDocViewController *viewController;

@end

