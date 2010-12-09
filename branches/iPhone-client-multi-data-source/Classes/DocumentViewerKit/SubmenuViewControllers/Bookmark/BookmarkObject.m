//
//  BookmarkObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookmarkObject.h"

@implementation BookmarkObject

@synthesize documentId;
@synthesize page;
@synthesize contentName;

+ (BookmarkObject *)objectWithDictionary:(NSDictionary *)dictionary {
    BookmarkObject *ret = [[BookmarkObject new] autorelease];
    
    ret.documentId = [dictionary objectForKey:@"documentId"];
    ret.page = [[dictionary objectForKey:@"page"] intValue];
    ret.contentName = [dictionary objectForKey:@"contentName"];
    
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:[NSString stringWithFormat:@"%@" , documentId] forKey:@"documentId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , page] forKey:@"page"];
    [ret setObject:contentName forKey:@"contentName"];
    
    return ret;
}

- (BOOL)equals:(BookmarkObject *)obj {
    return [self.documentId compare:obj.documentId] == NSOrderedSame && self.page == obj.page;
}

- (void)dealloc {
    [contentName release];
	[documentId release];
    
    [super dealloc];
}

@end
