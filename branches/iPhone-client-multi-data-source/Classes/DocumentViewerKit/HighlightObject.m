//
//  HighlightObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/09/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "HighlightObject.h"

@implementation HighlightObject

@synthesize location;
@synthesize length;
@synthesize color;
@synthesize text;

- (void)dealloc {
    [text release];
    
    [super dealloc];
}

+ (HighlightObject *)objectWithDictionary:(NSDictionary *)dictionary {
    HighlightObject *ret = [[HighlightObject new] autorelease];
    
    ret.location = [[dictionary objectForKey:@"location"] intValue];
    ret.length = [[dictionary objectForKey:@"length"] intValue];
    ret.color = [[dictionary objectForKey:@"color"] intValue];
    ret.text = [dictionary objectForKey:@"text"];
    
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:[NSString stringWithFormat:@"%d" , location] forKey:@"location"];
    [ret setObject:[NSString stringWithFormat:@"%d" , length] forKey:@"length"];
    [ret setObject:[NSString stringWithFormat:@"%d" , color] forKey:@"color"];
    [ret setObject:text forKey:@"text"];
    
    return ret;
}

- (NSRange)range {
    return NSMakeRange(location, length);
}

@end
