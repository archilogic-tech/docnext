//
//  RegionInfo.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RegionInfo.h"

@implementation RegionInfo

@synthesize region;
@synthesize index;

- (void)dealloc {
    [region release];
    
    [super dealloc];
}

@end
