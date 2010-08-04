//
//  RegionManager.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/03.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "OverlayManager.h"
#import "FileUtil.h"
#import "NSString+Data.h"
#import "JSON.h"
#import "RangeObject.h"
#import "UIBalloon.h"

@implementation OverlayManager

@synthesize markerView;
@synthesize scrollView;
@synthesize balloonContainerView;

#pragma mark lifecycle

- (void)dealloc {
    [regions release];
    [separationHolder release];
    [selectionLeft release];
    [selectionRight release];
    
    [super dealloc];
}

#pragma mark private

- (CGRect)calcActualRect:(double)ratio size:(CGSize)size {
    CGRect ret = CGRectMake(0, 0, size.width, size.height);
    
    if ( ret.size.width < ret.size.height * ratio ) {
        // fit to width
        ret.origin.y = (ret.size.height - ret.size.width / ratio) / 2.0;
        ret.size.height = ret.size.width / ratio;
    } else {
        // fit to height
        ret.origin.x = (ret.size.width - ret.size.height * ratio) / 2.0;
        ret.size.width = ret.size.height * ratio;
    }
    
    return ret;
}

- (Region *)region:(int)atIndex {
    if ( regions == nil ) {
        regions = [[FileUtil regions:docId page:page] retain];
    }
    
    return [regions objectAtIndex:atIndex];
}

- (int)getNearestIndex:(CGPoint)point {
    if ( regions == nil ) {
        regions = [[FileUtil regions:docId page:page] retain];
    }
    if ( separationHolder == nil ) {
        separationHolder = [[SeparationHolder alloc] initWithRegions:regions];
    }
    
    double x = MAX(MIN((point.x - actual.origin.x) / actual.size.width , 1) , 0);
    double y = MAX(MIN((point.y - actual.origin.y) / actual.size.height , 1) , 0);
    
    return [separationHolder nearestIndex:CGPointMake(x , y)];
}

- (CGRect)convertToStageRect:(Region *)region {
    return CGRectMake(actual.origin.x + region.x * actual.size.width,
                      actual.origin.y + region.y * actual.size.height,
                      region.width * actual.size.width,
                      region.height * actual.size.height);
}

- (CGPoint)convertToStagePoint:(CGPoint)point {
    return CGPointMake(actual.origin.x + point.x * actual.size.width,
                       actual.origin.y + point.y * actual.size.height);
}

- (void)addSelectionMarker:(int)count regionBase:(int)regionBase markerBase:(int)markerBase {
    for ( int delta = 0 ; delta < count ; delta++ ) {
        [markerView addSelectionMarker:[self convertToStageRect:[self region:(regionBase + delta)]]
                               atIndex:(markerBase + (markerBase >= 0 ? delta : 0))];
    }
}

- (void)removeSelectionMarker:(int)count markerIndex:(int)markerIndex {
    for ( int delta = 0 ; delta < count ; delta++ ) {
        [markerView removeSelectionMarker:markerIndex];
    }
}

- (IBAction)dragSelection:(UIScaleButton *)sender andEvent:(UIEvent *)event {
    CGPoint point = [[[event touchesForView:sender] anyObject] locationInView:markerView];
    int index = [self getNearestIndex:point];
    
    if (sender.isLeft ?
        (index != selectionMinIndex && index <= selectionMaxIndex) :
        (index != selectionMaxIndex && index >= selectionMinIndex) ) {
        
        Region *nearest = [self region:index];
        [sender moveToTip:[self convertToStagePoint:
                           CGPointMake(nearest.x + (sender.isLeft ? 0 : nearest.width),
                                       nearest.y + nearest.height / 2)] scale:scrollView.zoomScale];
        
        if ( sender.isLeft ) {
            if ( index < selectionMinIndex ) {
                [self addSelectionMarker:(selectionMinIndex - index) regionBase:index markerBase:0];
            } else {
                [self removeSelectionMarker:(index - selectionMinIndex) markerIndex:0];
            }
            
            selectionMinIndex = index;
        } else {
            if ( index > selectionMaxIndex ) {
                [self addSelectionMarker:(index - selectionMaxIndex) regionBase:(selectionMaxIndex + 1) markerBase:-1];
            } else {
                [self removeSelectionMarker:(selectionMaxIndex - index) markerIndex:-1];
            }
            
            selectionMaxIndex = index;
        }
    }
}

- (void)showSelectionMarkerForMin:(int)minIndex max:(int)maxIndex {
    [markerView clearSelectionMarker];
    [selectionLeft removeFromSuperview];
    [selectionLeft release];
    [selectionRight removeFromSuperview];
    [selectionRight release];
    
    for ( int index = minIndex ; index <= maxIndex ; index++ ) {
        [markerView addSelectionMarker:[self convertToStageRect:[regions objectAtIndex:index]] atIndex:(index - minIndex)];
    }
    
    Region *leftRegion = [regions objectAtIndex:minIndex];
    selectionLeft = [[UIScaleButton alloc] initWithTip:
                     [self convertToStagePoint:CGPointMake(leftRegion.x, leftRegion.y + leftRegion.height / 2)]
                                                isLeft:YES scale:scrollView.zoomScale];
    [selectionLeft addTarget:self
                      action:@selector(dragSelection:andEvent:) forControlEvents:(UIControlEventTouchDragInside | UIControlEventTouchDragOutside)];
    [scrollView addSubview:selectionLeft];
    
    selectionRight = [[UIScaleButton alloc] initWithTip:
                      [self convertToStagePoint:CGPointMake(leftRegion.x + leftRegion.width, leftRegion.y + leftRegion.height / 2)]
                                                 isLeft:NO scale:scrollView.zoomScale];
    [selectionRight addTarget:self
                       action:@selector(dragSelection:andEvent:) forControlEvents:(UIControlEventTouchDragInside | UIControlEventTouchDragOutside)];
    [scrollView addSubview:selectionRight];
    
    selectionMinIndex = minIndex;
    selectionMaxIndex = maxIndex;
}

#pragma mark public

- (void)setParam:(int)_docId page:(int)_page size:(CGSize)size {
    docId = _docId;
    page = _page;
    
    // clear cache
    [separationHolder release];
    separationHolder = nil;
    [regions release];
    regions = nil;
    
    // TODO cache ratio or not?
    double ratio = [[[[NSString stringWithData:
                       [FileUtil read:
                        [NSString stringWithFormat:@"%d/info.json" , docId]]] JSONValue] objectForKey:@"ratio"] doubleValue];
    actual = [self calcActualRect:ratio size:size];
}

#pragma mark Selection

- (void)selectNearest:(CGPoint)point {
    int index = [self getNearestIndex:point];
    [self showSelectionMarkerForMin:index max:index];
}

#pragma mark Balloon

- (void)addBalloon:(NSString *)text tip:(CGPoint)tip {
    [balloonContainerView addSubview:[[[UIBalloon alloc] initWithText:text tip:tip] autorelease]];
}

#pragma mark SearchResult

- (void)showSearchResult:(NSArray *)ranges selectedIndex:(int)selectedIndex {
    [markerView clearSearchResultMarker];
    
    for ( int index = 0 ; index < ranges.count ; index++ ) {
        RangeObject *range = [ranges objectAtIndex:index];
        for ( int delta = 0 ; delta < range.length ; delta++ ) {
            Region *region = [self region:(range.location + delta)];
            [markerView addSearchResultMarker:[self convertToStageRect:region] selected:(index == selectedIndex)];
            
            if ( index == selectedIndex && delta == range.length / 2 ) {
                [self addBalloon:@"Selected" tip:[self convertToStagePoint:CGPointMake(region.x, region.y)]];
            }
        }
    }
}

#pragma mark etc

- (void)applyScaleView:(float)scale {
    for ( UIView *view in balloonContainerView.subviews ) {
        [(UIBalloon *)view adjustForTip:scale];
    }
    
    [selectionLeft adjustForTip:scale];
    [selectionRight adjustForTip:scale];
}

@end
