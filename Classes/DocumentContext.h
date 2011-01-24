//
//  DocumentContext.h
//  MapDoc
//
//  Created by sakukawa on 11/01/19.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Region.h"

@protocol DocumentViewerDatasource;

@interface DocumentContext : NSObject {
	id<NSObject,DocumentViewerDatasource> _datasource;

	NSArray *_documentId;
	int _currentPage;					// データファイル上のページ
	int _currentIndex;					// アプリで扱うページ

	UIInterfaceOrientation _currentOrientation;

	int _totalPage;
	NSArray *_singlePageInfo;
	NSArray *_pageHeads;
	NSArray *_isSinglePage;


	NSDictionary *_metaDocumentInfoCache;
}

@property (nonatomic, retain) id<NSObject> documentId;
@property (nonatomic) int documentOffset;
@property (nonatomic) int currentPage;
@property (nonatomic) int currentIndex;

- (BOOL)isEqualToMetaDocumentId:(id<NSObject>)did;

+ (DocumentContext *)objectWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)toDictionary;

- (id)init;
- (void)dealloc;
- (void)didInterfaceOrientationChanged:(NSNotification*)n;
- (id<NSObject>)documentId;
- (void)setDocumentId:(id <NSObject>)docId;
- (BOOL)isValidIndex:(int)i;
- (BOOL)isValidPage:(int)i;
- (int)currentPageByIndex:(int)i;
- (int)currentIndexByPage:(int)page;
- (void)setCurrentPage:(int)n;
- (void)setCurrentIndex:(int)n;
- (int)totalPageWithDocumentOffset:(int)i;
- (int)totalPage;
- (BOOL)isSinglePage;
- (NSArray*)titles;
- (NSString*)titleWithPage:(int)index;
- (NSString*)title;
- (NSString*)texts;
- (NSArray*)freehand;
- (NSArray*)annotations;
- (NSArray*)highlights;
- (NSArray*)region;
- (double)ratio;
- (void)loadSinglePageInfo;
- (UIImage*)thumbnailWithIndex:(int)index;
- (BOOL)isSinglePage:(int)page;
- (void)buildPageHeads;
- (UIView*)getTileImageWithType:(NSString*)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution;
- (NSString*)publisher;

@end
