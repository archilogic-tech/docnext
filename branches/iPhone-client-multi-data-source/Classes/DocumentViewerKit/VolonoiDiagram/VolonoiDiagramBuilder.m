//
//  VolonoiDiagramBuilder.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "VolonoiDiagramBuilder.h"
#import "Region.h"

@implementation VolonoiDiagramBuilder

+ (NSArray *)build:(NSArray *)regions width:(int)width height:(int)height actual:(CGRect)actual {
    NSDate *date = [NSDate date];
    
    NSMutableArray *ret = [NSMutableArray arrayWithCapacity:height];
    
    for ( int y = 0 ; y < height ; y++ ) {
        NSLog(@"y:%d",y);
        NSMutableArray *line = [NSMutableArray arrayWithCapacity:width];
        
        for ( int x = 0 ; x < width ; x++ ) {
            double minDist = DBL_MAX;
            int minIndex = -1;
            for ( int index = 0 ; index < regions.count ; index++ ) {
                Region *region = [regions objectAtIndex:index];
                
                double dx = pow(actual.origin.x + (region.x + region.width / 2) * actual.size.width - x, 2);
                double dy = pow(actual.origin.y + (region.y + region.height / 2) * actual.size.height- y , 2);
                double dist = dx + dy;

                if ( dist < minDist ) {
                    minDist = dist;
                    minIndex = index;
                }
            }
            
            [line addObject:[NSNumber numberWithInt:minIndex]];
        }
        
        [ret addObject:line];
    }
    
    NSLog(@"Volonoi build tooks: %f",[date timeIntervalSinceNow]);
    
    return ret;
}

@end
