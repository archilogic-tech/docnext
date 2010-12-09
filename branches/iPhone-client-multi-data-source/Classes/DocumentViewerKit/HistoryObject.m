//
//  HistoryObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "HistoryObject.h"

@implementation HistoryObject

@synthesize documentId;
@synthesize page;

+ (HistoryObject *)objectWithDictionary:(NSDictionary *)dictionary {
    HistoryObject *ret = [[HistoryObject new] autorelease];
    
    ret.documentId = [dictionary objectForKey:@"documentId"];
    ret.page = [[dictionary objectForKey:@"page"] intValue];
    
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:[NSString stringWithFormat:@"%@" , documentId] forKey:@"documentId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , page] forKey:@"page"];
    
    return ret;
}

@end
