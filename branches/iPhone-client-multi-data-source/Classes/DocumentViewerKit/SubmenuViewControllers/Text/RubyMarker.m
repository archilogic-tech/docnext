//
//  RubyObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RubyMarker.h"

@implementation RubyMarker

@synthesize text;
@synthesize range;

+ (RubyMarker *) markerWithData:(NSString *)text location:(int)location length:(int)length {
    RubyMarker *ret = [[RubyMarker new] autorelease];
    
    ret.text = text;
    ret.range = NSMakeRange( location , length );
    
    return ret;
}

+ (RubyMarker *) markerWithDictionary:(NSDictionary *)dictionary {
    return [RubyMarker markerWithData:[dictionary objectForKey:@"text"]
                             location:[[dictionary objectForKey:@"location"] intValue]
                               length:[[dictionary objectForKey:@"length"] intValue]];
}

- (void)dealloc {
    [text release];
    
    [super dealloc];
}

@end
