//
//  ThumbnailView.h
//  MapDoc
//
//  Created by sakukawa on 11/01/24.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FlowCoverView.h"

@interface ThumbnailView : UIView {
	IBOutlet FlowCoverView *flowCoverView;
    IBOutlet UILabel *titleLabel;
    IBOutlet UILabel *pageLabel;
    IBOutlet UISlider *pageSlider;
}

@property(nonatomic,retain) IBOutlet FlowCoverView *flowCoverView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UILabel *pageLabel;
@property(nonatomic,retain) IBOutlet UISlider *pageSlider;

@end
