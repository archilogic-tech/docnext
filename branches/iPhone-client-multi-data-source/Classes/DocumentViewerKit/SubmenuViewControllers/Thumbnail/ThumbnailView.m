//
//  ThumbnailView.m
//  MapDoc
//
//  Created by sakukawa on 11/01/24.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "ThumbnailView.h"


@implementation ThumbnailView

@synthesize flowCoverView;
@synthesize titleLabel;
@synthesize pageLabel;
@synthesize pageSlider;

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code.
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code.
}
*/

- (void)dealloc {
	[flowCoverView release];
    [titleLabel release];
    [pageLabel release];
    [pageSlider release];
	
    [super dealloc];
}


@end
