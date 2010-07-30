//
//  TilidScrollView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "Region.h"
#import "RegionInfo.h"

@class TapDetectingView;

@protocol TiledScrollViewDataSource;

@interface TiledScrollView : UIScrollView <UIScrollViewDelegate> {
    id<TiledScrollViewDataSource> dataSource;
    CGSize tileSize;
    TapDetectingView *tileContainerView;
    UIView *imageContainerView;
    UIView *markerContainerView;
    UIView *balloonContainerView;
    NSMutableSet *pendingTiles;

    int resolution;
    int maximumResolution;
    int minimumResolution;
    
    // we use the following ivars to keep track of which rows and columns are visible
    int firstVisibleRow, firstVisibleColumn, lastVisibleRow, lastVisibleColumn;

    BOOL isZoomChanging;
    float baseScale;
    
    float fromScale;
    float toScale;
    
    NSMutableArray *selectionMarkers;
    int selectionMinIndex;
    int selectionMaxIndex;
}

@property(nonatomic,assign) id <TiledScrollViewDataSource> dataSource;
@property(nonatomic) CGSize tileSize;
@property(nonatomic,retain) TapDetectingView *tileContainerView;
@property(nonatomic,retain) NSMutableSet *pendingTiles;
@property(nonatomic) int minimumResolution;
@property(nonatomic) int maximumResolution;
@property(nonatomic) float baseScale;

- (void)reloadDataWithNewContentSize:(CGSize)size;
- (void)releasePending;
- (void)clearMarker;
- (CGRect)calcActualRect:(double)ratio;
- (void)drawMarker:(Region *)region ratio:(double)ratio color:(UIColor *)color;
- (void)drawMarkerForSelect:(NSArray *)regions ratio:(double)ratio color:(UIColor *)color index:(int)index;
- (void)addBalloon:(NSString *)text tip:(CGPoint)tip ratio:(double)ratio;
- (void)applyScaleView;
@end


@protocol TiledScrollViewDataSource <NSObject>
- (UIView *)tiledScrollView:(TiledScrollView *)scrollView tileForRow:(int)row column:(int)column resolution:(int)resolution;
- (void)resetLoad;
- (void)movePage:(BOOL)isLeft;
- (void)beginDragging;
- (RegionInfo *)getNearestRegion:(CGPoint)point;
- (double)ratio;
- (Region *)getRegion:(int)index;
@end
