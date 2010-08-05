//
//  UIScaleButton.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "UIScaleButton.h"

#define UIScaleButtonWidth 80
#define UIScaleButtonHeight 80

@implementation UIScaleButton

@synthesize isLeft;

- (id)initWithTip:(CGPoint)_tip isLeft:(BOOL)_isLeft scale:(float)scale {
    tip = _tip;
    isLeft = _isLeft;
    
    if ( ( self = [super initWithFrame:CGRectZero] ) ) {
        [self setImage:[UIImage imageNamed:@"image_selection_indicator.png"] forState:UIControlStateNormal];

        [self adjustForTip:scale];
    }
    
    return self;
}

- (void)adjustForTip:(float)scale {
    self.frame = CGRectMake(tip.x * scale - UIScaleButtonWidth / 2, tip.y * scale - UIScaleButtonHeight / 2, UIScaleButtonWidth, UIScaleButtonHeight);
}

- (void)moveToTip:(CGPoint)_tip scale:(float)scale {
    tip = _tip;
    [self adjustForTip:scale];
}

- (void)setTouchOffset:(CGPoint)point {
    touchOffset = CGPointMake(point.x - UIScaleButtonWidth / 2, point.y - UIScaleButtonHeight / 2);
    NSLog(@"offset: %f %f",touchOffset.x,touchOffset.y);
}

- (CGPoint)getTouchOffset {
    return touchOffset;
}

@end
