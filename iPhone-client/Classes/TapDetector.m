//
//  TapDetectingView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "TapDetector.h"

#define DOUBLE_TAP_DELAY 0.5
#define LONG_TAP_DELAY 1.5

CGPoint midpointBetweenPoints(CGPoint a, CGPoint b);

@interface TapDetector ()
- (void)handleSingleTap;
- (void)handleDoubleTap;
- (void)handleTwoFingerTap;
- (void)handleSingleLongTap;
@end

@implementation TapDetector

@synthesize delegate;
@synthesize basisView;

- (id)init {
    self = [super init];
    if ( self ) {
        twoFingerTapIsPossible = YES;
        multipleTouches = NO;
    }
    return self;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    // cancel any pending handleSingleTap messages 
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(handleSingleTap) object:nil];
    
    // update our touch state
    if ( [[event touchesForView:basisView] count] > 1 ) {
        multipleTouches = YES;
    }
    if ( [[event touchesForView:basisView] count] > 2 ) {
        twoFingerTapIsPossible = NO;
    }
    if ( [[event touchesForView:basisView] count] == 0 ) {
        tapLocation = [[touches anyObject] locationInView:basisView];
        [self performSelector:@selector(handleSingleLongTap) withObject:nil afterDelay:LONG_TAP_DELAY];
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(handleSingleLongTap) object:nil];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    BOOL allTouchesEnded = ([touches count] == [[event touchesForView:basisView] count]);
    
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(handleSingleLongTap) object:nil];
    
    if ( !multipleTouches ) {
        // first check for plain single/double tap, which is only possible if we haven't seen multiple touches

        UITouch *touch = [touches anyObject];
        tapLocation = [touch locationInView:basisView];

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
                tapLocations[i] = [touch locationInView:basisView];
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
                tapLocation = [touch locationInView:basisView];
            } else {
                twoFingerTapIsPossible = NO;
            }
        }
        else if ( [touches count] == 1 && allTouchesEnded ) {
            // case 3: this is the end of the second of the two touches
            UITouch *touch = [touches anyObject];
            if ( [touch tapCount] == 1 ) {
                // if the last touch up is a single tap, this was a 2-finger tap
                tapLocation = midpointBetweenPoints( tapLocation , [touch locationInView:basisView] );
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
    if ( [delegate respondsToSelector:@selector(tapDetectorGotSingleTapAtPoint:)] ) {
        [delegate tapDetectorGotSingleTapAtPoint:tapLocation];
    }
}

- (void)handleDoubleTap {
    if ( [delegate respondsToSelector:@selector(tapDetectorGotDoubleTapAtPoint:)] ) {
        [delegate tapDetectorGotDoubleTapAtPoint:tapLocation];
    }
}
    
- (void)handleTwoFingerTap {
    if ( [delegate respondsToSelector:@selector(tapDetectorGotTwoFingerTapAtPoint:)] ) {
        [delegate tapDetectorGotTwoFingerTapAtPoint:tapLocation];
    }
}
    
- (void)handleSingleLongTap {
    if ( [delegate respondsToSelector:@selector(tapDetectorGotSingleLongTapAtPoint:)] ) {
        [delegate tapDetectorGotSingleLongTapAtPoint:tapLocation];
    }
}

@end

CGPoint midpointBetweenPoints(CGPoint a, CGPoint b) {
    CGFloat x = ( a.x + b.x ) / 2.0;
    CGFloat y = ( a.y + b.y ) / 2.0;
    return CGPointMake( x , y );
}
                    
