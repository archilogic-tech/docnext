//
//  ITextView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ITextView.h"

@implementation ITextView

@synthesize text;
@synthesize rubys;
@synthesize textSizes;
@synthesize config;

- (void)dealloc {
    [text release];
    [rubys release];
    [textSizes release];
    [config release];

    [super dealloc];
}


@end
