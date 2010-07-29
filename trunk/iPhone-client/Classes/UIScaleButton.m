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
        [self adjustForTip:scale];
        self.backgroundColor = [UIColor redColor];
    }
    
    return self;
}

- (void)adjustForTip:(float)scale {
    self.frame = CGRectMake(tip.x * scale - (isLeft ? UIScaleButtonWidth : 0), tip.y * scale - UIScaleButtonHeight / 2, UIScaleButtonWidth, UIScaleButtonHeight);
}

- (void)moveToTip:(CGPoint)_tip scale:(float)scale {
    tip = _tip;
    [self adjustForTip:scale];
}

@end
