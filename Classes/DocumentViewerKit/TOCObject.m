//
//  TOCObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TOCObject.h"

@implementation TOCObject

@synthesize page;
@synthesize text;

+ (TOCObject *)objectWithDictionary:(NSDictionary *)dictionary {
    TOCObject *ret = [[TOCObject new] autorelease];
    
    ret.page = [[dictionary objectForKey:@"page"] intValue];
    ret.text = [dictionary objectForKey:@"text"];
    
    return ret;
}

- (void)dealloc {
    [text release];
    
    [super dealloc];
}

@end
