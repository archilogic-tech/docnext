//
//  NSString+Data.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "NSString+Data.h"

@implementation NSString (DataAdditions)

+ (NSString *)stringWithData:(NSData *)data {
    char* buffer = malloc( [data length] + 1 );
    [data getBytes:buffer];
    buffer[ [data length] ] = 0;
    
    NSString *ret = [NSString stringWithUTF8String:buffer];
    
    free( buffer );
    
    return ret;
}

@end
