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

	ret.documentContext = [DocumentContext objectWithDictionary:dictionary];
    ret.contentName = [dictionary objectForKey:@"contentName"];
    
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];

	[ret addEntriesFromDictionary:[_documentContext toDictionary]];
    [ret setObject:contentName forKey:@"contentName"];
    
    return ret;
}

- (BOOL)equals:(BookmarkObject *)obj {
	// TODO
	return ([_documentContext isEqual:obj.documentContext]);
}

- (void)dealloc {
    [contentName release];
	[_documentContext release];
    
    [super dealloc];
}

@end
