//
//  RegionManager.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/03.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SeparationHolder.h"
#import "Region.h"
#import "UIScaleButton.h"
#import "MarkerView.h"

@protocol OverlayManagerDelegate

- (void)didBeginSelect;
- (void)didEndSelect;
- (void)didTouchDownHighlight:(int)serial;

@end

@interface OverlayManager : NSObject {
    int docId;
    int page;
    
    // cache values
    NSArray *regions;
    SeparationHolder *separationHolder;
    CGRect actual;
    
    id<OverlayManagerDelegate> delegate;
    int selectionMinIndex;
    int selectionMaxIndex;
    UIScaleButton *selectionLeft;
    UIScaleButton *selectionRight;
    
    NSMutableDictionary *highlightBalloons;
    
    // umm....
    UIScrollView *scrollView;
    MarkerView *markerView;
    UIView *balloonContainerView;
}

@property(nonatomic,assign) id<OverlayManagerDelegate> delegate;
@property(nonatomic,assign) UIScrollView *scrollView;
@property(nonatomic,assign) MarkerView *markerView;
@property(nonatomic,assign) UIView *balloonContainerView;

- (void)setParam:(int)docId page:(int)page size:(CGSize)size;
- (void)selectNearest:(CGPoint)point;
- (BOOL)hasSelection;
- (NSRange)selection;
- (void)clearSelection;
- (UIView *)addBalloon:(NSString *)text tip:(CGPoint)tip;
- (void)showSearchResult:(NSArray *)ranges selectedIndex:(int)selectedIndex;
- (int)showHighlight:(NSRange)range color:(UIColor *)color selecting:(BOOL)selecting;
- (void)clearHighlightSelection;
- (void)changeHighlightComment:(int)serial text:(NSString *)text;
- (void)changeHighlightColor:(int)serial color:(UIColor *)color;
- (void)deleteHighlight:(int)serial;
- (void)applyScaleView:(float)scale;

@end
