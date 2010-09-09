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

- (UIControl *)addControlMarker:(CGRect)rect color:(UIColor *)color {
    UIControl *view = [[[UIControl alloc] initWithFrame:CGRectMake(rect.origin.x, rect.origin.y, rect.size.width,
                                                                   rect.size.height)] autorelease];
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

- (UIControl *)addHighlightMarker:(CGRect)rect color:(UIColor *)color serial:(int)serial {
    id key = [NSNumber numberWithInt:serial];
    
    if ( ![highlights objectForKey:key] ) {
        [highlights setObject:[NSMutableArray arrayWithCapacity:0] forKey:key];
    }
    
    UIControl *marker = [self addControlMarker:rect color:color];
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

@end
