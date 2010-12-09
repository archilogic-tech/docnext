//
//  TextSizeMarker.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TextSizeMarker.h"

@implementation TextSizeMarker

@synthesize factor;
@synthesize line;

+ (TextSizeMarker *) markerWithData:(float)factor line:(int)line {
    TextSizeMarker *ret = [[TextSizeMarker new] autorelease];
    
    ret.factor = factor;
    ret.line = line;
    
    return ret;
}

+ (TextSizeMarker *)markerWithDictionary:(NSDictionary *)dictionary {
    return [TextSizeMarker markerWithData:[[dictionary objectForKey:@"factor"] floatValue]
                                     line:[[dictionary objectForKey:@"line"] intValue]];
}

@end
