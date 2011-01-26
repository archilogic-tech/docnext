//
//  DocumentSearchResult.h
//  MapDoc
//
//  Created by sakukawa on 11/01/26.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface DocumentSearchResult : NSObject {
	int _page;
	NSArray *_ranges;
}

@property (nonatomic) int page;
@property (nonatomic, readonly) NSArray *ranges;

+ (DocumentSearchResult*)documentSearchResultWithPage:(int)page ranges:(NSMutableArray*)ranges;
- (id)initWithPage:(int)page ranges:(NSArray*)ranges;

@end
