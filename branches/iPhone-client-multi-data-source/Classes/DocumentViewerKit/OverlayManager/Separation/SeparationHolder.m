//
//  SeparationHolder.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "SeparationHolder.h"
#import "Region.h"
#import "IntDoublePair.h"

#define SeparationHolderNSegment 128

@implementation SeparationHolder

+ (double)cx:(Region *)region {
    return region.x + region.width / 2.0;
}

+ (double)cy:(Region *)region {
    return region.y + region.height / 2.0;
}

- (IntDoublePair *)findNearestInSeparation:(CGPoint)point px:(int)px py:(int)py {
    double minDist = DBL_MAX;
    int minIndex = -1;
    
    for ( int index = 0 ; index < [[[regionHashMap objectAtIndex:py] objectAtIndex:px] count] ; index++ ) {
        Region *region = [[[regionHashMap objectAtIndex:py] objectAtIndex:px] objectAtIndex:index];

        double dx = pow([SeparationHolder cx:region] - point.x , 2);
        double dy = pow([SeparationHolder cy:region] - point.y , 2);
        double dist = dx + dy;
        if ( dist < minDist ) {
            minDist = dist;
            minIndex = [[[[indexHashMap objectAtIndex:py] objectAtIndex:px] objectAtIndex:index] intValue];
        }
    }
    
    return [IntDoublePair pairWithIntValue:minIndex doubleValue:minDist];
}

- (id)initWithRegions:(NSArray *)regions {
    regionHashMap = [[NSMutableArray arrayWithCapacity:SeparationHolderNSegment] retain];
    indexHashMap = [[NSMutableArray arrayWithCapacity:SeparationHolderNSegment] retain];
    
    for ( int y = 0 ; y < SeparationHolderNSegment ; y++ ) {
        NSMutableArray *regionLine = [NSMutableArray arrayWithCapacity:SeparationHolderNSegment];
        NSMutableArray *indexLine = [NSMutableArray arrayWithCapacity:SeparationHolderNSegment];
        
        for ( int x = 0 ; x < SeparationHolderNSegment ; x++ ) {
            NSMutableArray *regionElem = [NSMutableArray arrayWithCapacity:0];
            [regionLine addObject:regionElem];

            NSMutableArray *indexElem = [NSMutableArray arrayWithCapacity:0];
            [indexLine addObject:indexElem];
        }
        
        [regionHashMap addObject:regionLine];
        [indexHashMap addObject:indexLine];
    }

    for ( int index = 0 ; index < regions.count ; index++ ) {
        Region *region = [regions objectAtIndex:index];

        double cx = [SeparationHolder cx:region];
        double cy = [SeparationHolder cy:region];
        
        if ( cx < 0 || cx >= 1 || cy < 0 || cy >= 1 ) {
            continue;
        }

        int px = cx * SeparationHolderNSegment;
        int py = cy * SeparationHolderNSegment;
        
        [[[regionHashMap objectAtIndex:py] objectAtIndex:px] addObject:region];
        [[[indexHashMap objectAtIndex:py] objectAtIndex:px] addObject:[NSNumber numberWithInt:index]];
    }
    
    return self;
}

- (int)nearestIndex:(CGPoint)point {
    int px = point.x * SeparationHolderNSegment;
    int py = point.y * SeparationHolderNSegment;
    
    double minDist = DBL_MAX;
    double minIndex = -1;
    for ( int delta = 0 ; delta < SeparationHolderNSegment ; delta++ ) {
        for ( int dy = -delta ; dy <= delta ; dy++ ) {
            if ( py + dy >= 0 && py + dy < SeparationHolderNSegment ) {
                for ( int dx = -delta ; dx <= delta ; dx++ ) {
                    if ( px + dx >= 0 && px + dx < SeparationHolderNSegment ) {
                        if ( abs( dx ) == delta || abs( dy ) == delta ) {
                            IntDoublePair *res = [self findNearestInSeparation:point px:(px + dx) py:(py + dy)];
                            if ( res.doubleValue < minDist ) {
                                minDist = res.doubleValue;
                                minIndex = res.intValue;
                            }
                        }
                    }
                }
            }
        }
        
        if ( delta > 0 && minIndex != -1 ) {
            return minIndex;
        }
    }
    
    return 0;
}

- (void)dealloc {
    [regionHashMap release];
    [indexHashMap release];
    
    [super dealloc];
}

@end
