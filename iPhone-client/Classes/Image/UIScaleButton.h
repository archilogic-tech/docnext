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
}

@property(nonatomic) BOOL isLeft;

- (id)initWithTip:(CGPoint)_tip isLeft:(BOOL)_isLeft scale:(float)scale;
- (void)moveToTip:(CGPoint)_tip scale:(float)scale;

@end
