//
//  UITouchAwareWindow.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "UITouchAwareWindow.h"

@implementation UITouchAwareWindow

@synthesize touchesObserver;

- (void)sendEvent:(UIEvent *)event {
	[super sendEvent:event];
	
	// only if it is a touch event type and we have observer
	if ((touchesObserver) && (event.type == UIEventTypeTouches)) {
        NSMutableSet *beganTouches = [NSMutableSet setWithCapacity:0];
        NSMutableSet *cancelTouches = [NSMutableSet setWithCapacity:0];
        NSMutableSet *endTouches = [NSMutableSet setWithCapacity:0];
        NSMutableSet *moveTouches = [NSMutableSet setWithCapacity:0];
		
		// fill in sets
		for (UITouch * touch in [event allTouches]) {
			if (touch.phase == UITouchPhaseBegan) {
                [beganTouches addObject:touch];
			} else if (touch.phase == UITouchPhaseCancelled) {
                [cancelTouches addObject:touch];
			} else if (touch.phase == UITouchPhaseMoved) {
                [moveTouches addObject:touch];
			} else if (touch.phase == UITouchPhaseEnded) {
                [endTouches addObject:touch];
            }
		}
		
		// call methods
		if ([beganTouches count] > 0) {
            [touchesObserver touchesBegan:beganTouches withEvent:event];
        }
		if ([cancelTouches count] > 0) {
            [touchesObserver touchesCancelled:cancelTouches withEvent:event];
        }
		if ([moveTouches count] > 0) {
            [touchesObserver touchesMoved:moveTouches withEvent:event];
        }
		if ([endTouches count] > 0) {
            [touchesObserver touchesEnded:endTouches withEvent:event];
        }
	}
}

@end
