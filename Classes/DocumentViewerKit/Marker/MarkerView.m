//
//  MarkerView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "MarkerView.h"

@implementation MarkerView

#pragma mark lifecycle

- (id)initWithFrame:(CGRect)frame {
    if ((self = [super initWithFrame:frame])) {
        searchResults = [[NSMutableSet setWithCapacity:0] retain];
        selections = [[NSMutableArray arrayWithCapacity:0] retain];
        highlights = [[NSMutableDictionary dictionaryWithCapacity:0] retain];
    }

    return self;
}

- (void)dealloc {
    [searchResults release];
    [selections release];
    [highlights release];
    
    [super dealloc];
}

#pragma mark private

- (UIView *)addMarker:(CGRect)rect color:(UIColor *)color {
    UIView *view = [[[UIView alloc] initWithFrame:rect] autorelease];
    view.backgroundColor = color;
    
    [self addSubview:view];
    
    return view;
}

- (UIHighlightIndicator *)addHighlightMarker:(CGRect)rect color:(UIColor *)color {
    UIHighlightIndicator *view = [[[UIHighlightIndicator alloc] initWithFrame:rect] autorelease];
    view.backgroundColor = color;
    
    [self addSubview:view];
    
    return view;
}

- (void)highlightTouchDown {
    NSLog(@"high touchdown");
}

#pragma mark public

- (void)addSelectionMarker:(CGRect)rect atIndex:(int)atIndex {
    if ( atIndex < 0 ) {
        // +1 to insert last
        atIndex += selections.count + 1;
    }
    
    [selections insertObject:[self addMarker:rect color:[[UIColor blueColor] colorWithAlphaComponent:0.5]] atIndex:atIndex];
}

- (void)removeSelectionMarker:(int)atIndex {
    if ( atIndex < 0 ) {
        atIndex += selections.count;
    }

    [[selections objectAtIndex:atIndex] removeFromSuperview];
    [selections removeObjectAtIndex:atIndex];
}

- (void)clearSelectionMarker {
    for ( UIView *view in selections ) {
        [view removeFromSuperview];
    }
    
    [selections removeAllObjects];
}

- (void)addSearchResultMarker:(CGRect)rect selected:(BOOL)selected {
    [searchResults addObject:[self addMarker:rect color:[(selected ? [UIColor redColor] : [UIColor yellowColor])
                                                         colorWithAlphaComponent:0.5]]];
}

- (void)clearSearchResultMarker {
    for ( UIView *view in searchResults ) {
        [view removeFromSuperview];
    }
    
    [searchResults removeAllObjects];
}

- (UIHighlightIndicator *)addHighlightMarker:(CGRect)rect color:(UIColor *)color serial:(int)serial {
    id key = [NSNumber numberWithInt:serial];
    
    if ( ![highlights objectForKey:key] ) {
        [highlights setObject:[NSMutableArray arrayWithCapacity:0] forKey:key];
    }
    
    UIHighlightIndicator *marker = [self addHighlightMarker:rect color:color];
    marker.serial = serial;
    
    [[highlights objectForKey:key] addObject:marker];
    
    return marker;
}

- (int)getHighlightNextSerial {
    for ( int serial = 0 ; ; serial++ ) {
        if ( ![highlights objectForKey:[NSNumber numberWithInt:serial]] ) {
            return serial;
        }
    }
}

- (void)setHighlightSelected:(int)serial {
    for ( NSNumber *key in highlights ) {
        float alp = [key intValue] == serial ? 0.9 : 0.5;
        //float width = [key intValue] == serial ? 2 : 0;
        for ( UIView *view in [highlights objectForKey:key] ) {
            view.backgroundColor = [view.backgroundColor colorWithAlphaComponent:alp];
            //view.layer.borderWidth = width;
            //view.layer.borderColor = [[UIColor redColor] CGColor];
        }
    }
}

- (void)changeHighlightColor:(int)serial color:(UIColor *)color {
    for ( UIView *view in [highlights objectForKey:[NSNumber numberWithInt:serial]] ) {
        view.backgroundColor = color;
    }
}

- (void)deleteHighlight:(int)serial {
    NSNumber *key = [NSNumber numberWithInt:serial];
    
    NSMutableArray *target = [highlights objectForKey:key];
    for ( UIView *view in target ) {
        [view removeFromSuperview];
    }
    
    [target removeAllObjects];
    
    [highlights removeObjectForKey:key];
}

- (CGPoint)calcHighlightTip:(int)serial {
    NSArray *target = [highlights objectForKey:[NSNumber numberWithInt:serial]];
    
    float top = FLT_MIN;
    float bottom = FLT_MAX;
    for ( int index = 0 ; index < [target count] ; index++ ) {
        UIView *view = [target objectAtIndex:index];
        
        if ( view.frame.origin.y > bottom || view.frame.origin.y + view.frame.size.height < top ) {
            UIView *mid = [target objectAtIndex:((index - 1) / 2)];
            return CGPointMake(mid.frame.origin.x + mid.frame.size.width / 2, mid.frame.origin.y);
        }
        
        top = view.frame.origin.y;
        bottom = view.frame.origin.y + view.frame.size.height;
    }
    
    UIView *mid = [target objectAtIndex:([target count] / 2)];
    return CGPointMake(mid.frame.origin.x + mid.frame.size.width / 2, mid.frame.origin.y);
}

- (UIURILinkIndicator *)addURILink:(CGRect)rect {
    UIURILinkIndicator *view = [[[UIURILinkIndicator alloc] initWithFrame:rect] autorelease];

    view.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.2];
    
    [self addSubview:view];
    
    return view;
}

- (UIGoToPageLinkIndicator *)addGoToPageLink:(CGRect)rect {
    UIGoToPageLinkIndicator *view = [[[UIGoToPageLinkIndicator alloc] initWithFrame:rect] autorelease];
    
    view.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.2];
    
    [self addSubview:view];
    
    return view;
}

@end
