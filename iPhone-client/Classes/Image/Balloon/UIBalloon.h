//
//  UIBalloon.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/27.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface UIBalloon : UIView {
    CGPoint tip;
    CGPoint tipOffset;
}

- (id)initWithText:(NSString *)text tip:(CGPoint)tip;
- (void)adjustForTip:(float)scale;

@end
