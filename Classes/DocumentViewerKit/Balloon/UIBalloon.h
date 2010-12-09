//
//  UIBalloon.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/27.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IScale.h"

@interface UIBalloon : UIView <IScale> {
    CGPoint tip;
    CGPoint tipOffset;
}

- (id)initWithText:(NSString *)text tip:(CGPoint)tip;

@end
