//
//  NSStringSearch.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "NSString+Search.h"
#import "RangeObject.h"

@implementation NSString (SearchAdditions)

- (NSArray *)search:(NSString *)term {
    if ( [term length] == 0 ) {
        return [NSArray array];
    }
    
    NSMutableArray *ret = [NSMutableArray arrayWithCapacity:0];
    
    NSRange range = NSMakeRange(0, [self length]);
    while ( YES ) {
        NSRange res = [self rangeOfString:term options:0 range:range];
        if ( res.location == NSNotFound ) {
            break;
        }
        
        [ret addObject:[RangeObject range:res]];
        
        int loc = res.location + res.length;
        range = NSMakeRange(loc, [self length] - loc);
    }
    
    return ret;
}

@end
