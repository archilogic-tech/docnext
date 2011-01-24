//
//  MapDocViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TiledScrollView.h"
#import "DocumentViewerDatasource.h"

@interface MapDocViewController : UINavigationController<DownloadManagerDelegate> {
    UIInterfaceOrientation willInterfaceOrientation;
	id<NSObject,DocumentViewerDatasource> _datasource;

	UIProgressView *_downloadProgressView;
    UIAlertView *_loading;
}

@property(nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;

@end
