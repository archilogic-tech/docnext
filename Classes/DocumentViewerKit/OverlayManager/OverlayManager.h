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
#import "DocumentViewerDatasource.h"
#import "DocumentContext.h"

@protocol OverlayManagerDelegate

- (void)didBeginSelect;
- (void)didEndSelect;
- (void)didTouchDownHighlight:(int)serial;
- (void)didTouchDownURILink:(NSString *)uri;
- (void)didTouchDownGoToPageLink:(int)page;

@end

@interface OverlayManager : NSObject {
	DocumentContext *_documentContext;
    
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
	
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,assign) id<OverlayManagerDelegate> delegate;
@property(nonatomic,assign) UIScrollView *scrollView;
@property(nonatomic,assign) MarkerView *markerView;
@property(nonatomic,assign) UIView *balloonContainerView;


@property(nonatomic, retain) DocumentContext *documentContext;

@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;

- (void)setParam:(DocumentContext*)documentContext size:(CGSize)size;
- (BOOL)selectNearest:(CGPoint)point;
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

- (void)addURILink:(Region *)region uri:(NSString *)uri;
- (void)addGoToPageLink:(Region *)region page:(int)page;
- (void)applyScaleView:(float)scale;

@end
