//
//  ThumbnailViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FlowCoverView.h"
#import "MapDocViewController.h"
#import "DocumentContext.h"

@interface ThumbnailViewController : UIViewController <FlowCoverViewDelegate> {

	// from IUIViewController
    UIProgressView *progressView;

	FlowCoverView *flowCoverView;
    UILabel *titleLabel;
    UILabel *pageLabel;
    UISlider *pageSlider;
    
    NSTimer *timer;
	DocumentContext *_documentContext;
	id<NSObject,DocumentViewerDatasource> _datasource;

	//    id<NSObject> documentId;
	//    int page;
}


@property(nonatomic,retain) IBOutlet FlowCoverView *flowCoverView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UILabel *pageLabel;
@property(nonatomic,retain) IBOutlet UISlider *pageSlider;

@property (nonatomic, retain) DocumentContext *documentContext;
@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (ThumbnailViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource;

- (IBAction)pageSliderChanged:(id)sender;

@end
