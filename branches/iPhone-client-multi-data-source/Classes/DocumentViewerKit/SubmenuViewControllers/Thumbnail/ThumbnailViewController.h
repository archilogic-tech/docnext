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

@interface ThumbnailViewController : IUIViewController <FlowCoverViewDelegate> {
    FlowCoverView *flowCoverView;
    UILabel *titleLabel;
    UILabel *pageLabel;
    UISlider *pageSlider;
    
    NSTimer *timer;
    id<NSObject> documentId;
    int page;
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet FlowCoverView *flowCoverView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UILabel *pageLabel;
@property(nonatomic,retain) IBOutlet UISlider *pageSlider;
@property(nonatomic,copy) id<NSObject> documentId;
@property(nonatomic) int page;

@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (ThumbnailViewController *)createViewController:(UIInterfaceOrientation)orientation datasource:(id<DocumentViewerDatasource>)datasource;

- (IBAction)pageSliderChanged:(id)sender;

@end
