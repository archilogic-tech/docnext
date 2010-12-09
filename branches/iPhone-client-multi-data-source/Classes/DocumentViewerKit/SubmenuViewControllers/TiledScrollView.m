//
//  TilideScrollView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "TiledScrollView.h"

#define TiledScrollViewSlideThresholdRatio (0.05)

@interface TiledScrollView ()
- (void)updateResolution;
@end

@implementation TiledScrollView

@synthesize zoomableContainerView;
@synthesize dataSource;
@synthesize tileSize;
@synthesize maximumResolution;
@synthesize baseScale;

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        zoomableContainerView = [[UIView alloc] initWithFrame:CGRectZero];
        [self addSubview:zoomableContainerView];
        
        tileContainerView = [[UIView alloc] initWithFrame:CGRectZero];
        tileContainerView.backgroundColor = [UIColor grayColor];
        [zoomableContainerView addSubview:tileContainerView];

        tileSize = CGSizeZero;
        
        firstVisibleRow = firstVisibleColumn = NSIntegerMax;
        lastVisibleRow  = lastVisibleColumn  = NSIntegerMin;
                
        super.delegate = self;
    }
    
    return self;
}

- (void)dealloc {
    [zoomableContainerView release];
    [tileContainerView release];

    [super dealloc];
}

- (void)reloadData {
    for (UIView *view in [tileContainerView subviews]) {
        if ( view.tag != TiledScrollViewTileLocal || resolution == 0 ) {
            [view removeFromSuperview];
        }
    }
    
    firstVisibleRow = firstVisibleColumn = NSIntegerMax;
    lastVisibleRow  = lastVisibleColumn  = NSIntegerMin;
    
    [self setNeedsLayout];
}

- (void)setContentSizeProperty:(CGSize)size {
    self.zoomScale = 1.0;
    self.minimumZoomScale = 1.0;
    self.maximumZoomScale = 1.0;
    resolution = 0;
    
    // +1 for horizontal dragging
    self.contentSize = CGSizeMake(size.width + 1 , size.height);
    
    zoomableContainerView.frame = CGRectMake(0 , 0 , size.width , size.height);
    tileContainerView.frame = CGRectMake(0 , 0 , size.width , size.height);
    
    [self reloadData];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    if ( isZoomChanging ) {
        [dataSource tiledScrollViewScaleChanging:self.zoomScale];
        return;
    }
    
    CGRect visibleBounds = self.bounds;

    // first remove all tiles that are no longer visible
    for (UIView *tile in [tileContainerView subviews]) {
        CGRect scaledTileFrame = [tileContainerView convertRect:tile.frame toView:self];

        // second condition for landscape view
        if ( !CGRectIntersectsRect(scaledTileFrame , visibleBounds) && tile.tag != TiledScrollViewTileLocal ) {
            [tile removeFromSuperview];
        }
    }
    
    // calculate which rows and columns are visible by doing a bunch of math.
    float power = pow(2 , resolution);
    float scaledTileWidth = tileSize.width * self.zoomScale / power;
    float scaledTileHeight = tileSize.height * self.zoomScale / power;
    int maxRow = floorf(zoomableContainerView.frame.size.height / scaledTileHeight);
    int maxCol = floorf(zoomableContainerView.frame.size.width  / scaledTileWidth);
    int firstNeededRow = MAX(0 , floorf(visibleBounds.origin.y / scaledTileHeight));
    int firstNeededCol = MAX(0 , floorf(visibleBounds.origin.x / scaledTileWidth));
    int lastNeededRow = MIN(maxRow , floorf(CGRectGetMaxY(visibleBounds) / scaledTileHeight));
    int lastNeededCol = MIN(maxCol , floorf(CGRectGetMaxX(visibleBounds) / scaledTileWidth));

    // iterate through needed rows and columns, adding any tiles that are missing
    for ( int row = firstNeededRow ; row <= lastNeededRow ; row++ ) {
        for ( int col = firstNeededCol ; col <= lastNeededCol ; col++ ) {
            BOOL tileIsMissing = (firstVisibleRow > row || firstVisibleColumn > col ||
                                  lastVisibleRow  < row || lastVisibleColumn  < col);
            
            if ( tileIsMissing ) {
                UIView *tile = [dataSource tiledScrollViewGetTile:self row:row column:col resolution:resolution];
                
                tile.layer.borderWidth = 1;
                tile.layer.borderColor = [[UIColor grayColor] CGColor];

                // set the tile's frame so we insert it at the correct position
                tile.frame = CGRectMake(tileSize.width / power * col , tileSize.height / power * row ,
                                        tileSize.width / power , tileSize.height / power);
                
                [tileContainerView addSubview:tile];
            }
        }
    }
    
    // update our record of which rows/cols are visible
    firstVisibleRow = firstNeededRow;
    firstVisibleColumn = firstNeededCol;
    lastVisibleRow  = lastNeededRow;
    lastVisibleColumn  = lastNeededCol;
    
    // +1 for horizontal dragging
    self.contentSize = CGSizeMake(zoomableContainerView.frame.size.width + 1 , zoomableContainerView.frame.size.height);
}


- (void)updateResolution {
    // fit to 0 < resolution < maximum
    int newResolution = MAX(MIN(ceil(log(self.zoomScale * baseScale) / log(2)) , maximumResolution) , 0);

    if ( newResolution != resolution ) {
        resolution = newResolution;
        [self reloadData];
    }
}
        
#pragma mark UIScrollViewDelegate

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return zoomableContainerView;
}

- (void)scrollViewDidEndZooming:(UIScrollView *)scrollView withView:(UIView *)view atScale:(float)scale {
    [self updateResolution];

    isZoomChanging = NO;
}

- (void)scrollViewWillBeginZooming:(UIScrollView *)scrollView withView:(UIView *)view {
    isZoomChanging = YES;
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    int threashold = scrollView.frame.size.width * TiledScrollViewSlideThresholdRatio;
    
    if ( scrollView.contentOffset.x < -threashold ) {
        [dataSource tiledScrollViewDidSlide:TiledScrollViewSlideDirectionLeft];
    }
    if ( scrollView.contentSize.width - ( scrollView.contentOffset.x + scrollView.frame.size.width ) < -threashold ) {
        [dataSource tiledScrollViewDidSlide:TiledScrollViewSlideDirectionRight];
    }
}

#pragma mark UIScrollView overrides

- (void)setZoomScale:(float)scale animated:(BOOL)animated {
    [super setZoomScale:scale animated:animated];
    
    if (!animated) {
        [self updateResolution];
    }
}

- (void)setDelegate:(id)delegate {
    if ( delegate ) {
        NSLog(@"You can't set the delegate of a TiledScrollView. It is its own delegate.");
    }
}

@end
