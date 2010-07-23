//
//  Region.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "Region.h"

@implementation Region

@synthesize x;
@synthesize y;
@synthesize width;
@synthesize height;

+ (Region *)objectWithDictionary:(NSDictionary *)dictionary {
    Region *ret = [[Region new] autorelease];
    
    ret.x = [[dictionary objectForKey:@"x"] doubleValue];
    ret.y = [[dictionary objectForKey:@"y"] doubleValue];
    ret.width = [[dictionary objectForKey:@"width"] doubleValue];
    ret.height = [[dictionary objectForKey:@"height"] doubleValue];
    
    return ret;
}

@end
