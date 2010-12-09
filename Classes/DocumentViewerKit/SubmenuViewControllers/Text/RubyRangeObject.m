//
//  RubyRangeObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/05.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RubyRangeObject.h"

@implementation RubyRangeObject

@synthesize base;
@synthesize begin;
@synthesize end;

+ (RubyRangeObject *)objectWithData:(float)base begin:(float)begin end:(float)end {
    RubyRangeObject *ret = [[RubyRangeObject new] autorelease];
    
    ret.base = base;
    ret.begin = begin;
    ret.end = end;

    return ret;
}

@end
