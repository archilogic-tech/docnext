//
//  ObjPoint.m
//  FreehandDrawing
//
//  Created by Yoskaku Toyama on 10/09/17.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ObjPoint.h"

@implementation ObjPoint

@synthesize x = _x;
@synthesize y = _y;

+ (ObjPoint *)pointFromCGPoint:(CGPoint)point {
    ObjPoint *ret = [[ObjPoint new] autorelease];
    
    ret.x = point.x;
    ret.y = point.y;
    
    return ret;
}

+ (ObjPoint *)pointFromDictionary:(NSDictionary *)dictionary {
    ObjPoint *ret = [[ObjPoint new] autorelease];
    
    ret.x = [[dictionary objectForKey:@"x"] floatValue];
    ret.y = [[dictionary objectForKey:@"y"] floatValue];
    
    return ret;
}

- (CGPoint)toCGPoint {
    return CGPointMake(_x, _y);
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:[NSNumber numberWithFloat:_x] forKey:@"x"];
    [ret setObject:[NSNumber numberWithFloat:_y] forKey:@"y"];
    
    return ret;
}

@end
