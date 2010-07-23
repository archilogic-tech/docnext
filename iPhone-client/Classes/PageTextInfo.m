//
//  PageTextInfo.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PageTextInfo.h"
#import "Region.h"

@implementation PageTextInfo

@synthesize text;
@synthesize regions;

+ (PageTextInfo *)objectWithDictionary:(NSDictionary *)dictionary {
    PageTextInfo *ret = [[PageTextInfo new] autorelease];
    
    NSLog(@"%d %d" , [dictionary isKindOfClass:[NSDictionary class]],[dictionary isKindOfClass:[NSArray class]]);
    
    ret.text = [dictionary objectForKey:@"text"];
    
    NSMutableArray *regions = [NSMutableArray arrayWithCapacity:0];
    for ( NSDictionary *dic in [dictionary objectForKey:@"regions"] ) {
        [regions addObject:[Region objectWithDictionary:dic]];
    }
    ret.regions = regions;
    
    return ret;
}

- (void)dealloc {
    [text release];
    [regions release];
    
    [super dealloc];
}

@end
