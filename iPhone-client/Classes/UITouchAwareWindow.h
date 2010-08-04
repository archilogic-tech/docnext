//
//  UITouchAwareWindow.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface UITouchAwareWindow : UIWindow {
    UIResponder *touchesObserver;
}

@property(nonatomic,assign) UIResponder *touchesObserver;

@end
