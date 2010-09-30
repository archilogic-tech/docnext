//
//  UIURILinkIndicator.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/09/14.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "UIURILinkIndicator.h"

@implementation UIURILinkIndicator

@synthesize uri;

- (void)dealloc {
    [uri release];
    
    [super dealloc];
}

@end
