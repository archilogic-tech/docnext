//
//  SearchResult.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/23.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "SearchResult.h"

@implementation SearchResult

@synthesize range;
@synthesize highlight;

- (void)dealloc {
    [range release];
    [highlight release];
    
    [super dealloc];
}

@end
