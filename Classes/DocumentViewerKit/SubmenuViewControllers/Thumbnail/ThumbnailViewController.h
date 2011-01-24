//
//  ThumbnailViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentContext.h"
#import "ThumbnailView.h"

@interface ThumbnailViewController : UIViewController <FlowCoverViewDelegate> {

	IBOutlet ThumbnailView *_landscapeView;
	IBOutlet ThumbnailView *_portraitView;
	
    NSTimer *timer;
	
	DocumentContext *_documentContext;
	id<NSObject,DocumentViewerDatasource> _datasource;
}


@property (nonatomic, retain) DocumentContext *documentContext;
@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (ThumbnailViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource;

- (IBAction)pageSliderChanged:(id)sender;

@end
