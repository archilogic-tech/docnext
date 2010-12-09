//
//  MarkupParseResult.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/05.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "MarkupParseResult.h"

@implementation MarkupParseResult

@synthesize text;
@synthesize rubys;
@synthesize textSizes;

- (void)dealloc {
    [text release];
    [rubys release];
    [textSizes release];
    
    [super dealloc];
}

@end
