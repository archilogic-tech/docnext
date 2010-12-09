//
//  MapDocAppDelegate.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UITouchAwareWindow.h"
#import "SampleDatasource.h"
#import "UstDocDatasource.h"

@class MapDocViewController;

@interface MapDocAppDelegate : NSObject <UIApplicationDelegate,DownloadManagerDelegate> {
	UITouchAwareWindow *window;
    MapDocViewController *viewController;
    
    UIAlertView *loading;
	UstDocDatasource *_datasource;
}

@property (nonatomic, retain) IBOutlet UITouchAwareWindow *window;
@property (nonatomic, retain) IBOutlet MapDocViewController *viewController;

@end

