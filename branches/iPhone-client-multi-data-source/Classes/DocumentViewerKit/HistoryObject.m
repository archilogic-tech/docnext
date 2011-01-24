//
//  HistoryObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "HistoryObject.h"

@implementation HistoryObject

@synthesize documentContext = _documentContext;

// TODO このクラスは、DocumentContextに近いので、廃止する

+ (HistoryObject *)objectWithDictionary:(NSDictionary *)dictionary {
    HistoryObject *ret = [[HistoryObject new] autorelease];

	DocumentContext *dc = [[DocumentContext alloc] init];
	dc.documentId = [dictionary objectForKey:@"documentId"];
	dc.currentPage = [[dictionary objectForKey:@"page"] intValue];
    ret.documentContext = dc;
	[dc release];

    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:[NSString stringWithFormat:@"%@" , _documentContext.documentId] forKey:@"documentId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , _documentContext.currentPage] forKey:@"page"];
    //[ret setObject:[NSString stringWithFormat:@"%d" , _documentContext.documentOffset] forKey:@"documentOffset"];
    
    return ret;
}

@end
