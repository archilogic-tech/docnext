//
//  TilidScrollView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#define TiledScrollViewTileLocal (0xdeadbeef)

@class TiledScrollView;

typedef enum {
    TiledScrollViewSlideDirectionUp,
    TiledScrollViewSlideDirectionDown,
    TiledScrollViewSlideDirectionLeft,
    TiledScrollViewSlideDirectionRight
} TiledScrollViewSlideDirection;

@protocol TiledScrollViewDataSource

- (UIView *)tiledScrollViewGetTile:(TiledScrollView *)scrollView row:(int)row column:(int)column resolution:(int)resolution;
- (void)tiledScrollViewDidSlide:(TiledScrollViewSlideDirection)direction;
- (void)tiledScrollViewScaleChanging:(float)scale;

@end

@interface TiledScrollView : UIScrollView <UIScrollViewDelegate> {
    UIView *zoomableContainerView;
    UIView *tileContainerView;

    id<TiledScrollViewDataSource> dataSource;
    CGSize tileSize;

    int resolution;
    int maximumResolution;
    
    int firstVisibleRow;
    int firstVisibleColumn;
    int lastVisibleRow;
    int lastVisibleColumn;

    BOOL isZoomChanging;
    float baseScale;
}

@property(nonatomic,readonly) UIView *zoomableContainerView;
@property(nonatomic,assign) id <TiledScrollViewDataSource> dataSource;
@property(nonatomic) CGSize tileSize;
@property(nonatomic) int maximumResolution;
@property(nonatomic) float baseScale;

- (void)setContentSizeProperty:(CGSize)size;

@end
