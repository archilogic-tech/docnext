//
//  TapDetectingView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

@protocol TapDetectorDelegate

@optional
- (void)tapDetectorGotSingleTapAtPoint:(CGPoint)tapPoint;
- (void)tapDetectorGotDoubleTapAtPoint:(CGPoint)tapPoint;
- (void)tapDetectorGotTwoFingerTapAtPoint:(CGPoint)tapPoint;
- (void)tapDetectorGotSingleLongTapAtPoint:(CGPoint)tapPoint;

@end

@interface TapDetector : NSObject {
    NSObject<TapDetectorDelegate> *delegate;
    UIView *basisView;
    
    // Touch detection
    CGPoint tapLocation;         // Needed to record location of setHideConfigView tap, which will only be registered after delayed perform.
    BOOL multipleTouches;        // YES if a touch event contains more than one touch; reset when all fingers are lifted.
    BOOL twoFingerTapIsPossible; // Set to NO when 2-finger tap can be ruled out (e.g. 3rd finger down, fingers touch down too far apart, etc).
    BOOL checkingLongTap;
}

@property(nonatomic,assign) NSObject<TapDetectorDelegate> *delegate;
@property(nonatomic,assign) UIView *basisView;

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event;

@end
