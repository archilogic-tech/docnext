//
//  UIScaleButton.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IScale.h"

@interface UIScaleButton : UIButton <IScale> {
    CGPoint tip;
    BOOL isLeft;
    CGPoint touchOffset;
}

@property(nonatomic) BOOL isLeft;

- (id)initWithTip:(CGPoint)tip isLeft:(BOOL)isLeft scale:(float)scale;
- (void)moveToTip:(CGPoint)tip scale:(float)scale;
- (void)setTouchOffset:(CGPoint)point;
- (CGPoint)getTouchOffset;

@end
