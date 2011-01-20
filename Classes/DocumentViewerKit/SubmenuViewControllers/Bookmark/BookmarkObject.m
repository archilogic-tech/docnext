//
//  BookmarkObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookmarkObject.h"

@implementation BookmarkObject

@synthesize contentName;
@synthesize documentContext = _documentContext;

+ (BookmarkObject *)objectWithDictionary:(NSDictionary *)dictionary {
    BookmarkObject *ret = [[BookmarkObject new] autorelease];

	DocumentContext *dc = [[DocumentContext alloc] init];
	dc.documentId = [dictionary objectForKey:@"documentId"];
	dc.currentPage = [[dictionary objectForKey:@"page"] intValue];
	ret.documentContext = dc;
	[dc release];
	
    ret.contentName = [dictionary objectForKey:@"contentName"];
    
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];

	
    [ret setObject:[NSString stringWithFormat:@"%@" , _documentContext.documentId] forKey:@"documentId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , _documentContext.currentPage] forKey:@"page"];
    [ret setObject:contentName forKey:@"contentName"];
    
    return ret;
}

- (BOOL)equals:(BookmarkObject *)obj {

	return ([_documentContext isEqual:obj.documentContext]);
//    return ([_documentContext compare:obj.documentContext] == NSOrderedSame);
	
//	return [self.documentId compare:obj.documentId] == NSOrderedSame && self.page == obj.page;
}

- (void)dealloc {
    [contentName release];
	[_documentContext release];
    
    [super dealloc];
}

@end
