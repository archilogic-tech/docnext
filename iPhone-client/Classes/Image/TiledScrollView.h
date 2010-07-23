//
//  TilidScrollView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

@class TapDetectingView;

@protocol TiledScrollViewDataSource;

@interface TiledScrollView : UIScrollView <UIScrollViewDelegate> {
    id<TiledScrollViewDataSource> dataSource;
    CGSize tileSize;
    TapDetectingView *tileContainerView;
    UIView *imageContainerView;
    UIView *markerContainerView;
    NSMutableSet *pendingTiles;

    int resolution;
    int maximumResolution;
    int minimumResolution;
    
    // we use the following ivars to keep track of which rows and columns are visible
    int firstVisibleRow, firstVisibleColumn, lastVisibleRow, lastVisibleColumn;

    BOOL isZoomChanging;
    float baseScale;
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
- (void)drawMarker:(NSArray *)regions ratio:(double)ratio;
@end


@protocol TiledScrollViewDataSource <NSObject>
- (UIView *)tiledScrollView:(TiledScrollView *)scrollView tileForRow:(int)row column:(int)column resolution:(int)resolution;
- (void)resetLoad;
- (void)movePage:(BOOL)isLeft;
- (void)beginDragging;
@end


