//
//  TapDetectingView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "TapDetectingView.h"

#define DOUBLE_TAP_DELAY 0.5
#define LONG_TAP_DELAY 1.5

CGPoint midpointBetweenPoints(CGPoint a, CGPoint b);

@interface TapDetectingView ()
- (void)handleSingleTap;
- (void)handleDoubleTap;
- (void)handleTwoFingerTap;
- (void)handleSingleLongTap;
@end

@implementation TapDetectingView
@synthesize delegate;

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.userInteractionEnabled = YES;
        self.multipleTouchEnabled = YES;
        twoFingerTapIsPossible = YES;
        multipleTouches = NO;
    }
    return self;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    // cancel any pending handleSingleTap messages 
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(handleSingleTap) object:nil];
    
    NSLog(@"touchesForView.count: %d",[[event touchesForView:self] count]);
    // update our touch state
    if ( [[event touchesForView:self] count] > 1 ) {
        multipleTouches = YES;
    }
    if ( [[event touchesForView:self] count] > 2 ) {
        twoFingerTapIsPossible = NO;
    }
    if ( [[event touchesForView:self] count] == 0 ) {
        tapLocation = [[touches anyObject] locationInView:self];
        [self performSelector:@selector(handleSingleLongTap) withObject:nil afterDelay:LONG_TAP_DELAY];
    }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    BOOL allTouchesEnded = ([touches count] == [[event touchesForView:self] count]);
    
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(handleSingleLongTap) object:nil];
    
    if ( !multipleTouches ) {
        // first check for plain single/double tap, which is only possible if we haven't seen multiple touches

        UITouch *touch = [touches anyObject];
        tapLocation = [touch locationInView:self];

        if ( [touch tapCount] == 1 ) {
            [self performSelector:@selector(handleSingleTap) withObject:nil afterDelay:DOUBLE_TAP_DELAY];
        } else if( [touch tapCount] == 2 ) {
            [self handleDoubleTap];
        }
    } else if ( multipleTouches && twoFingerTapIsPossible ) { 
        // check for 2-finger tap if we've seen multiple touches and haven't yet ruled out that possibility
        
        if ( [touches count] == 2 && allTouchesEnded ) {
            // case 1: this is the end of both touches at once 
            int i = 0;
            int tapCounts[2];
            CGPoint tapLocations[2];
            
            for ( UITouch *touch in touches ) {
                tapCounts[i]    = [touch tapCount];
                tapLocations[i] = [touch locationInView:self];
                i++;
            }
            if ( tapCounts[0] == 1 && tapCounts[1] == 1 ) { // it's a two-finger tap if they're both single taps
                tapLocation = midpointBetweenPoints( tapLocations[0] , tapLocations[1] );
                [self handleTwoFingerTap];
            }
        } else if ( [touches count] == 1 && !allTouchesEnded ) {
            // case 2: this is the end of one touch, and the other hasn't ended yet
            UITouch *touch = [touches anyObject];
            if ( [touch tapCount] == 1 ) {
                // if touch is a single tap, store its location so we can average it with the second touch location
                tapLocation = [touch locationInView:self];
            } else {
                twoFingerTapIsPossible = NO;
            }
        }
        else if ( [touches count] == 1 && allTouchesEnded ) {
            // case 3: this is the end of the second of the two touches
            UITouch *touch = [touches anyObject];
            if ( [touch tapCount] == 1 ) {
                // if the last touch up is a single tap, this was a 2-finger tap
                tapLocation = midpointBetweenPoints( tapLocation , [touch locationInView:self] );
                [self handleTwoFingerTap];
            }
        }
    }
        
    // if all touches are up, reset touch monitoring state
    if ( allTouchesEnded ) {
        twoFingerTapIsPossible = YES;
        multipleTouches = NO;
    }
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event {
    twoFingerTapIsPossible = YES;
    multipleTouches = NO;
}

#pragma mark Private

- (void)handleSingleTap {
    if ( [delegate respondsToSelector:@selector(tapDetectingView:gotSingleTapAtPoint:)] ) {
        [delegate tapDetectingView:self gotSingleTapAtPoint:tapLocation];
    }
}

- (void)handleDoubleTap {
    if ( [delegate respondsToSelector:@selector(tapDetectingView:gotDoubleTapAtPoint:)] ) {
        [delegate tapDetectingView:self gotDoubleTapAtPoint:tapLocation];
    }
}
    
- (void)handleTwoFingerTap {
    if ( [delegate respondsToSelector:@selector(tapDetectingView:gotTwoFingerTapAtPoint:)] ) {
        [delegate tapDetectingView:self gotTwoFingerTapAtPoint:tapLocation];
    }
}
    
- (void)handleSingleLongTap {
    if ( [delegate respondsToSelector:@selector(tapDetectingView:gotSingleLongTapAtPoint:)] ) {
        [delegate tapDetectingView:self gotSingleLongTapAtPoint:tapLocation];
    }
}

@end

CGPoint midpointBetweenPoints(CGPoint a, CGPoint b) {
    CGFloat x = ( a.x + b.x ) / 2.0;
    CGFloat y = ( a.y + b.y ) / 2.0;
    return CGPointMake( x , y );
}
                    
