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
- (void)addBalloon:(NSString *)text tip:(CGPoint)tip;
- (void)showSearchResult:(NSArray *)ranges selectedIndex:(int)selectedIndex;
- (void)applyScaleView:(float)scale;
- (BOOL)hasSelection;
- (NSRange)selection;
- (void)clearSelection;

@end
