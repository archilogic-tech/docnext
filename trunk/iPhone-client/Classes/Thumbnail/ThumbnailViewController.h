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
    int documentId;
    int page;
}

@property(nonatomic,retain) IBOutlet FlowCoverView *flowCoverView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UILabel *pageLabel;
@property(nonatomic,retain) IBOutlet UISlider *pageSlider;
@property(nonatomic) int documentId;
@property(nonatomic) int page;

+ (ThumbnailViewController *)createViewController:(UIInterfaceOrientation)orientation docId:(int)documentId page:(int)page;

- (IBAction)pageSliderChanged:(id)sender;

@end
