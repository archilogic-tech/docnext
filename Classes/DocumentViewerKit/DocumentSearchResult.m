//
//  DocumentSearchResult.m
//  MapDoc
//
//  Created by sakukawa on 11/01/26.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "DocumentSearchResult.h"


@implementation DocumentSearchResult

@synthesize page = _page;
@synthesize ranges = _ranges;


+ (DocumentSearchResult*)documentSearchResultWithPage:(int)page ranges:(NSMutableArray*)ranges
{
	return [[[DocumentSearchResult alloc] initWithPage:page ranges:ranges] autorelease];
}

- (id)initWithPage:(int)page ranges:(NSArray*)ranges
{
	if ((self = [super init])) {
		_page = page;
		_ranges = [[NSArray alloc] initWithArray:ranges];
	}
	return self;
}

- (void)dealloc
{
	[_ranges release];
	[super dealloc];
}

@end
