//
//  MapDocAppDelegate.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentDownloadManager.h"

// for BrowserViewController
#define LibraryURL (@"http://ustdoc.com/docman_optimage/library.html")

// for UstDocDatasource
#define ServerEndpoint (@"http://ustdoc.com/docman_optimage/dispatch/viewer/")


@class MapDocViewController;

@interface MapDocAppDelegate : NSObject <UIApplicationDelegate> {
	UIWindow *window;
    MapDocViewController *viewController;
    
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet MapDocViewController *viewController;

@property (nonatomic, readonly) id<NSObject,DocumentViewerDatasource> datasource;


@end

