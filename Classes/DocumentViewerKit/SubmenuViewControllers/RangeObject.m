//
//  RangeObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RangeObject.h"


@implementation RangeObject

@synthesize location;
@synthesize length;

+ (RangeObject *)range:(NSRange)range {
    RangeObject *ret = [[RangeObject new] autorelease];
    
    ret.location = range.location;
    ret.length = range.length;
    
    return ret;
}

- (NSRange)range {
    return NSMakeRange(self.location, self.length);
}

@end
