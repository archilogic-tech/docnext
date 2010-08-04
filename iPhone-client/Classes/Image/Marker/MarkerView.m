//
//  MarkerView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "MarkerView.h"

@implementation MarkerView

#pragma mark lifecycle

- (id)initWithFrame:(CGRect)frame {
    if ((self = [super initWithFrame:frame])) {
        searchResults = [[NSMutableSet setWithCapacity:0] retain];
        selections = [[NSMutableArray arrayWithCapacity:0] retain];
    }

    return self;
}

- (void)dealloc {
    [searchResults release];
    [selections release];
    
    [super dealloc];
}

#pragma mark private

- (UIView *)addMarker:(CGRect)rect color:(UIColor *)color {
    UIView *view = [[[UIView alloc] initWithFrame:CGRectMake(rect.origin.x, rect.origin.y, rect.size.width,
                                                             rect.size.height)] autorelease];
    view.backgroundColor = color;

    [self addSubview:view];
    
    return view;
}

#pragma mark public

- (void)addSearchResultMarker:(CGRect)rect selected:(BOOL)selected {
    [searchResults addObject:[self addMarker:rect color:[(selected ? [UIColor redColor] : [UIColor yellowColor])
                                                         colorWithAlphaComponent:0.5]]];
}

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

- (void)clearSearchResultMarker {
    for ( UIView *view in searchResults ) {
        [view removeFromSuperview];
    }
    
    [searchResults removeAllObjects];
}

- (void)clearSelectionMarker {
    for ( UIView *view in selections ) {
        [view removeFromSuperview];
    }
    
    [selections removeAllObjects];
}

@end
