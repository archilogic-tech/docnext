//
//  DocumentContext.h
//  MapDoc
//
//  Created by sakukawa on 11/01/19.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Region.h"
//#import "DocumentViewerDatasource.h"

@protocol DocumentViewerDatasource;


@interface DocumentContext : NSObject {
	id<NSObject,DocumentViewerDatasource> _datasource;

	
	NSArray /*id<NSObject>*/ *_documentId;
//	int _documentOffset;
	int _currentPage;					// データファイル上のページ
	int _currentIndex;					// アプリで扱うページ

	UIInterfaceOrientation _currentOrientation;
	
//	NSArray *singlePageInfo;

	int _totalPage;
	NSArray *_singlePageInfo;
	NSArray *_pageHeads;
	NSArray *_isSinglePage;
}


- (BOOL)isSinglePage;

- (NSArray*)titles;
- (NSString*)titleWithIndex:(int)index;
- (NSString*)title;

- (NSArray*)region;
- (double)ratio;
- (NSString*)texts;
- (int)totalPage;

//- (NSComparisonResult)compare:(DocumentContext *)anotherDocumentContext;


@property (nonatomic, retain) id<NSObject> documentId;
@property (nonatomic) int documentOffset;
@property (nonatomic) int currentPage;
@property (nonatomic) int currentIndex;

- (BOOL)isValidIndex:(int)i;


@end
