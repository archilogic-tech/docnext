//
//  TilideScrollView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "TiledScrollView.h"
#import "TapDetectingView.h"
#import "UIRemoteImageView.h"
#import "Region.h"

@interface TiledScrollView ()
- (void)updateResolution;
@end

@implementation TiledScrollView

@synthesize dataSource;
@synthesize tileSize;
@synthesize tileContainerView;
@synthesize pendingTiles;
@dynamic minimumResolution;
@dynamic maximumResolution;
@synthesize baseScale;

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.pendingTiles = [[NSMutableSet new] autorelease];
        
        // we need a tile container view to hold all the tiles. This is the view that is returned
        // in the -viewForZoomingInScrollView: delegate method, and it also detects taps.
        tileContainerView = [[TapDetectingView alloc] initWithFrame:CGRectZero];
        self.tileContainerView.backgroundColor = [UIColor grayColor];
        
        [self addSubview:self.tileContainerView];
        [self setTileSize:CGSizeZero];
        
        imageContainerView = [[UIView alloc] initWithFrame:CGRectZero];
        [tileContainerView addSubview:imageContainerView];
        markerContainerView = [[UIView alloc] initWithFrame:CGRectZero];
        [tileContainerView addSubview:markerContainerView];

        // no rows or columns are visible at first; note this by making the firsts very high and the lasts very low
        firstVisibleRow = firstVisibleColumn = NSIntegerMax;
        lastVisibleRow  = lastVisibleColumn  = NSIntegerMin;
                
        // the TiledScrollView is its own UIScrollViewDelegate, so we can handle our own zooming.
        // We need to return our tileContainerView as the view for zooming, and we also need to receive
        // the scrollViewDidEndZooming: delegate callback so we can update our resolution.
        super.delegate = self;
    }
    
    return self;
}

- (void)dealloc {
    [pendingTiles release];
    [tileContainerView release];
    [imageContainerView release];
    [markerContainerView release];

    [super dealloc];
}

// we don't synthesize our minimum/maximum resolution accessor methods because we want to police the values of these ivars
- (int)minimumResolution { return minimumResolution; }
- (int)maximumResolution { return maximumResolution; }
- (void)setMinimumResolution:(int)res { minimumResolution = MIN(res, 0); } // you can't have a minimum resolution greater than 0
- (void)setMaximumResolution:(int)res { maximumResolution = MAX(res, 0); } // you can't have a maximum resolution less than 0

- (void)reloadData {
    for (UIView *view in [imageContainerView subviews]) {
        [pendingTiles addObject:view];
        
        if ( resolution == 0 ) {
            [view removeFromSuperview];
        }
    }
    
    // no rows or columns are now visible; note this by making the firsts very high and the lasts very low
    firstVisibleRow = firstVisibleColumn = NSIntegerMax;
    lastVisibleRow  = lastVisibleColumn  = NSIntegerMin;
    
    [self setNeedsLayout];
}

- (void)reloadDataWithNewContentSize:(CGSize)size {
    // since we may have changed resolutions, which changes our maximum and minimum zoom scales, we need to 
    // reset all those values. After calling this method, the caller should change the maximum/minimum zoom scales
    // if it wishes to permit zooming.
    
    self.zoomScale = 1.0;
    self.minimumZoomScale = 1.0;
    self.maximumZoomScale = 1.0;
    resolution = 0;
    
    // now that we've reset our zoom scale and resolution, we can safely set our contentSize. 
    // +1 for horizontal dragging
    self.contentSize = CGSizeMake( size.width + 1 , size.height );
    
    // we also need to change the frame of the tileContainerView so its size matches the contentSize
    self.tileContainerView.frame = CGRectMake( 0 , 0 , size.width , size.height );
    imageContainerView.frame = CGRectMake( 0 , 0 , size.width , size.height );
    markerContainerView.frame = CGRectMake( 0 , 0 , size.width , size.height );
    
    [self reloadData];
}

- (void)releasePending {
    NSLog(@"releasePending");
    
    for ( UIView *view in pendingTiles ) {
        if ( view.tag != 1 && view.tag != 2 ) {
            [view removeFromSuperview];
        }
    }
    
    [pendingTiles removeAllObjects];
}

- (void)drawMarker:(NSArray *)regions ratio:(double)ratio {
    for ( UIView *view in [markerContainerView subviews] ) {
        [view removeFromSuperview];
    }
    
    float w = self.frame.size.width;
    float h = self.frame.size.height;
    float left = 0.0;
    float top = 0.0;
    
    if ( w < h * ratio ) {
        // fit to width
        top = (h - w / ratio) / 2.0;
        h = w / ratio;
    } else {
        // fit to height
        left = (w - h * ratio) / 2.0;
        w = h * ratio;
    }
    
    for ( Region *region in regions ) {
        UIView *view = [[[UIView alloc] initWithFrame:CGRectMake(left + region.x * w, top + region.y * h, region.width * w, region.height * h)] autorelease];
        view.backgroundColor = [UIColor colorWithRed:1.0 green:0.0 blue:0.0 alpha:0.5];
        view.tag = 2;
        [markerContainerView addSubview:view];
    }
}

/***********************************************************************************/
/* Most of the work of tiling is done in layoutSubviews, which we override here.   */
/* We recycle the tiles that are no longer in the visible bounds of the scrollView */
/* and we add any tiles that should now be present but are missing.                */
/***********************************************************************************/
- (void)layoutSubviews {
    if ( isZoomChanging ) {
        return;
    }
    
    [super layoutSubviews];
    
    CGRect visibleBounds = self.bounds;

    // first remove all tiles that are no longer visible
    for (UIView *tile in [imageContainerView subviews]) {
        // We want to see if the tiles intersect our (i.e. the scrollView's) bounds, so we need to convert their
        // frames to our own coordinate system
        CGRect scaledTileFrame = [imageContainerView convertRect:tile.frame toView:self];

        // If the tile doesn't intersect, it's not visible, so we can remove it
        if ( !CGRectIntersectsRect( scaledTileFrame , visibleBounds ) && tile.tag != 1 ) {
            [tile removeFromSuperview];
        }
    }
    
    float p = pow( 2 , resolution );
    
    // calculate which rows and columns are visible by doing a bunch of math.
    float scaledTileWidth = self.tileSize.width * self.zoomScale / p;
    float scaledTileHeight = self.tileSize.height * self.zoomScale / p;
    int maxRow = floorf( tileContainerView.frame.size.height / scaledTileHeight ); // this is the maximum possible row
    int maxCol = floorf( tileContainerView.frame.size.width  / scaledTileWidth );  // and the maximum possible column
    int firstNeededRow = MAX( 0 , floorf( visibleBounds.origin.y / scaledTileHeight ) );
    int firstNeededCol = MAX( 0 , floorf( visibleBounds.origin.x / scaledTileWidth ) );
    int lastNeededRow = MIN( maxRow , floorf( CGRectGetMaxY( visibleBounds ) / scaledTileHeight ) );
    int lastNeededCol = MIN( maxCol , floorf( CGRectGetMaxX( visibleBounds ) / scaledTileWidth ) );

    [dataSource resetLoad];
    
    // iterate through needed rows and columns, adding any tiles that are missing
    for (int row = firstNeededRow; row <= lastNeededRow; row++) {
        for (int col = firstNeededCol; col <= lastNeededCol; col++) {
            BOOL tileIsMissing = (firstVisibleRow > row || firstVisibleColumn > col ||
                                  lastVisibleRow  < row || lastVisibleColumn  < col);
            
            if ( tileIsMissing ) {
                UIView *tile = [dataSource tiledScrollView:self tileForRow:row column:col resolution:resolution];
                
                tile.layer.borderWidth = 1;
                tile.layer.borderColor = [[UIColor grayColor] CGColor];

                // set the tile's frame so we insert it at the correct position
                tile.frame = CGRectMake( self.tileSize.width / p * col , self.tileSize.height / p * row , self.tileSize.width / p , self.tileSize.height / p );
                
                [imageContainerView addSubview:tile];
            }
        }
    }

    // update our record of which rows/cols are visible
    firstVisibleRow = firstNeededRow;
    firstVisibleColumn = firstNeededCol;
    lastVisibleRow  = lastNeededRow;
    lastVisibleColumn  = lastNeededCol;
    
    // +1 for horizontal dragging
    self.contentSize = CGSizeMake( self.tileContainerView.frame.size.width + 1 , self.tileContainerView.frame.size.height );
}


/*****************************************************************************************/
/* The following method handles changing the resolution of our tiles when our zoomScale  */
/* gets below 50% or above 100%. When we fall below 50%, we lower the resolution 1 step, */
/* and when we get above 100% we raise it 1 step. The resolution is stored as a power of */
/* 2, so -1 represents 50%, and 0 represents 100%, and so on.                            */
/*****************************************************************************************/
- (void)updateResolution {
    int newResolution = MAX( MIN( ceil( log( self.zoomScale * self.baseScale ) / log( 2 ) ) , maximumResolution ) , minimumResolution );

    if ( newResolution != resolution ) {
        NSLog(@"res: %d",newResolution);
        resolution = newResolution;
        
        [self reloadData];
    }
}
        
#pragma mark UIScrollViewDelegate

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return tileContainerView;
}

- (void)scrollViewDidEndZooming:(UIScrollView *)scrollView withView:(UIView *)view atScale:(float)scale {
    // after a zoom, check to see if we should change the resolution of our tiles
    [self updateResolution];

    // Little hack to enable horizontal dragging any time
    [super setZoomScale:scale+0.01 animated:NO];
    [super setZoomScale:scale animated:NO];
    
    isZoomChanging = NO;
}

- (void)scrollViewWillBeginZooming:(UIScrollView *)scrollView withView:(UIView *)view {
    isZoomChanging = YES;
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    [dataSource beginDragging];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    int threashold = scrollView.frame.size.width / 20;
    
    if ( scrollView.contentOffset.x < -threashold ) {
        [dataSource movePage:YES];
    }
    if ( scrollView.contentSize.width - ( scrollView.contentOffset.x + scrollView.frame.size.width ) < -threashold ) {
        [dataSource movePage:NO];
    }
}

#pragma mark UIScrollView overrides

// the scrollViewDidEndZooming: delegate method is only called after an *animated* zoom. We also need to update our 
// resolution for non-animated zooms. So we also override the new setZoomScale:animated: method on UIScrollView
- (void)setZoomScale:(float)scale animated:(BOOL)animated {
    [super setZoomScale:scale animated:animated];
    
    // the delegate callback will catch the animated case, so just cover the non-animated case
    if (!animated) {
        [self updateResolution];
    }
}

// We override the setDelegate: method because we can't manage resolution changes unless we are our own delegate.
- (void)setDelegate:(id)delegate {
    if ( delegate ) {
        NSLog(@"You can't set the delegate of a TiledScrollView. It is its own delegate.");
    }
}

@end
