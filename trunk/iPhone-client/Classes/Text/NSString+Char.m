//
//  NSString+Char.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "NSString+Char.h"

@implementation NSString (CharAdditions)

+ (NSString *)stringWithChar:(unichar)c {
    return [NSString stringWithCharacters:&c length:1];
}

- (NSString *)charAt:(int)index {
    return [NSString stringWithChar:[self characterAtIndex:index]];
}

@end
