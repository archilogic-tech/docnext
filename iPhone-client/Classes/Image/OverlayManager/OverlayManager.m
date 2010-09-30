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
#import "UIHighlightIndicator.h"
#import "UIURILinkIndicator.h"
#import "UIGoToPageLinkIndicator.h"

@implementation OverlayManager

@synthesize delegate;
@synthesize scrollView;
@synthesize markerView;
@synthesize balloonContainerView;

#pragma mark lifecycle

- (id)init {
    if ( self = [super init] ) {
        [self clearSelection];
        
        highlightBalloons = [[NSMutableDictionary dictionaryWithCapacity:0] retain];
    }
    
    return self;
}

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
    
    if ( regions.count == 0 ) {
        return -1;
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

- (IBAction)touchDownSelection:(UIScaleButton *)sender andEvent:(UIEvent *)event {
    [delegate didBeginSelect];
    
    [sender setTouchOffset:[[[event touchesForView:sender] anyObject] locationInView:sender]];
}

- (IBAction)touchUpSelection:(id)sender {
    [delegate didEndSelect];
}

- (IBAction)touchDragSelection:(UIScaleButton *)sender andEvent:(UIEvent *)event {
    CGPoint point = [[[event touchesForView:sender] anyObject] locationInView:markerView];
    CGPoint offset = [sender getTouchOffset];
    int index = [self getNearestIndex:CGPointMake(point.x - offset.x / scrollView.zoomScale, point.y - offset.y / scrollView.zoomScale)];
    
    if (sender.isLeft ?
        (index != selectionMinIndex && index <= selectionMaxIndex) :
        (index != selectionMaxIndex && index >= selectionMinIndex) ) {
        
        Region *nearest = [self region:index];
        [sender moveToTip:[self convertToStagePoint:
                           CGPointMake(nearest.x + (sender.isLeft ? 0 : nearest.width),
                                       nearest.y + (sender.isLeft ? 0 : nearest.height))] scale:scrollView.zoomScale];
        
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
    
    Region *region = [regions objectAtIndex:minIndex];
    selectionLeft = [[UIScaleButton alloc] initWithTip:
                     [self convertToStagePoint:CGPointMake(region.x, region.y)]
                                                isLeft:YES scale:scrollView.zoomScale];
    [selectionLeft addTarget:self action:@selector(touchDownSelection:andEvent:) forControlEvents:UIControlEventTouchDown];
    [selectionLeft addTarget:self action:@selector(touchDragSelection:andEvent:)
            forControlEvents:(UIControlEventTouchDragInside | UIControlEventTouchDragOutside)];
    [selectionLeft addTarget:self action:@selector(touchUpSelection:)
            forControlEvents:(UIControlEventTouchUpInside | UIControlEventTouchUpOutside)];
    [scrollView addSubview:selectionLeft];
    
    selectionRight = [[UIScaleButton alloc] initWithTip:
                      [self convertToStagePoint:CGPointMake(region.x + region.width, region.y + region.height)]
                                                 isLeft:NO scale:scrollView.zoomScale];
    [selectionRight addTarget:self action:@selector(touchDownSelection:andEvent:) forControlEvents:UIControlEventTouchDown];
    [selectionRight addTarget:self action:@selector(touchDragSelection:andEvent:)
             forControlEvents:(UIControlEventTouchDragInside | UIControlEventTouchDragOutside)];
    [selectionRight addTarget:self action:@selector(touchUpSelection:)
            forControlEvents:(UIControlEventTouchUpInside | UIControlEventTouchUpOutside)];
    [scrollView addSubview:selectionRight];
    
    selectionMinIndex = minIndex;
    selectionMaxIndex = maxIndex;
    
    [delegate didEndSelect];
}

- (void)touchDownHighlight:(UIHighlightIndicator *)sender {
    [markerView setHighlightSelected:sender.serial];
    
    [delegate didTouchDownHighlight:sender.serial];
}

- (void)touchDownURILink:(UIURILinkIndicator *)sender {
    [delegate didTouchDownURILink:sender.uri];
}

- (void)touchDownGoToPageLink:(UIGoToPageLinkIndicator *)sender {
    [delegate didTouchDownGoToPageLink:sender.page];
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

- (BOOL)selectNearest:(CGPoint)point {
    int index = [self getNearestIndex:point];
    
    if ( index == -1 ) {
        return NO;
    }
    
    [self showSelectionMarkerForMin:index max:index];
    
    return YES;
}

- (BOOL)hasSelection {
    return selectionMinIndex != -1 || selectionMaxIndex != -1;
}

- (NSRange)selection {
    if ( ![self hasSelection] ) {
        return NSMakeRange(NSNotFound, 0);
    }
    
    return NSMakeRange(selectionMinIndex, selectionMaxIndex - selectionMinIndex + 1);
}

- (void)clearSelection {
    [markerView clearSelectionMarker];
    [selectionLeft removeFromSuperview];
    [selectionRight removeFromSuperview];
    
    [selectionLeft release];
    selectionLeft = nil;
    [selectionRight release];
    selectionRight = nil;
    
    selectionMinIndex = -1;
    selectionMaxIndex = -1;
}

#pragma mark Balloon

- (UIView *)addBalloon:(NSString *)text tip:(CGPoint)tip {
    UIView *ret = [[[UIBalloon alloc] initWithText:text tip:tip] autorelease];
    
    [balloonContainerView addSubview:ret];
    
    return ret;
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

#pragma mark Highlight

- (int)showHighlight:(NSRange)range color:(UIColor *)color selecting:(BOOL)selecting {
    int serial = [markerView getHighlightNextSerial];

    for ( int delta = 0 ; delta < range.length ; delta++ ) {
        Region *region = [self region:(range.location + delta)];
        
        UIHighlightIndicator *marker = [markerView addHighlightMarker:[self convertToStageRect:region] color:color serial:serial];
        
        [marker addTarget:self action:@selector(touchDownHighlight:) forControlEvents:UIControlEventTouchDown];
    }
    
    if ( selecting ) {
        [markerView setHighlightSelected:serial];
    }
    
    return serial;
}

- (void)clearHighlightSelection {
    [markerView setHighlightSelected:-1];
}

- (void)changeHighlightComment:(int)serial text:(NSString *)text {
    NSNumber *key = [NSNumber numberWithInt:serial];
    
    if ( [highlightBalloons objectForKey:key] ) {
        [[highlightBalloons objectForKey:key] removeFromSuperview];
        [highlightBalloons removeObjectForKey:key];
    }
    
    if ( [text length] > 0 ) {
        [highlightBalloons setObject:[self addBalloon:text tip:[markerView calcHighlightTip:serial]] forKey:key];
    }
}

- (void)changeHighlightColor:(int)serial color:(UIColor *)color {
    [markerView changeHighlightColor:serial color:color];
}

- (void)deleteHighlight:(int)serial {
    [markerView deleteHighlight:serial];

    NSNumber *key = [NSNumber numberWithInt:serial];
    if ( [highlightBalloons objectForKey:key] ) {
        [[highlightBalloons objectForKey:key] removeFromSuperview];
        [highlightBalloons removeObjectForKey:key];
    }
}

#pragma mark Link

- (void)addURILink:(Region *)region uri:(NSString *)uri {
    UIURILinkIndicator *view = [markerView addURILink:[self convertToStageRect:region]];
    view.uri = uri;
    
    [view addTarget:self action:@selector(touchDownURILink:) forControlEvents:UIControlEventTouchDown];
}

- (void)addGoToPageLink:(Region *)region page:(int)_page {
    UIGoToPageLinkIndicator *view = [markerView addGoToPageLink:[self convertToStageRect:region]];
    view.page = _page;
    
    [view addTarget:self action:@selector(touchDownGoToPageLink:) forControlEvents:UIControlEventTouchDown];
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
