//
//  IntegerDoublePair.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "IntDoublePair.h"

@implementation IntDoublePair

@synthesize intValue;
@synthesize doubleValue;

+ (IntDoublePair *)pairWithIntValue:(int)intValue doubleValue:(double)doubleValue {
    IntDoublePair *ret = [[IntDoublePair new] autorelease];
    
    ret.intValue = intValue;
    ret.doubleValue = doubleValue;
    
    return ret;
}

@end
