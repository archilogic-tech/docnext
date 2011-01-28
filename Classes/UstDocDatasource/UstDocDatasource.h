//
//  UstDocDatasource.h
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DocumentViewerDatasource.h"
#import "StandardLocalStorageManager.h"
#import "UstDocDownloadManager.h"

#define ServerEndpoint (@"http://ustdoc.com/docman_optimage/dispatch/viewer/")
#define ReachabilityHost (@"ustdoc.com")

@class UstDocDownloadManager;

@interface UstDocDatasource : NSObject<DocumentViewerDatasource> {
	NSOperationQueue *_imageFetchQueue;
	UstDocDownloadManager *_downloadManager;
	StandardLocalStorageManager *_localStorage;

//	NSMutableDictionary *_imageCache;
}


// 外部向け
- (id)init;
- (void)dealloc;

- (void)didReceiveMemoryWarning;
- (void)updateSystemFromVersion:(NSString*)currentVersion toVersion:(NSString*)newVersion;

// ドキュメント操作系
- (BOOL)existsDocument:(id<NSObject>)metaDocumentId;
- (void)deleteCache:(id<NSObject>)metaDocumentId;


// ドキュメントメタ情報系
- (NSDictionary*)info:(id<NSObject>)metaDocumentId;
- (int)pages:(id<NSObject>)metaDocumentId;

- (NSDictionary*)info:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId;
//- (int)pages:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId;
- (double)ratio:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId;

- (NSArray *)tocs:(id<NSObject>)metaDocumentId;
//- (NSArray *)tocs:(id<NSObject>)metaDocumentId documentId:(id)documentId;
//- (TOCObject *)toc:(id<NSObject>)metaDocumentId documentId:(id)documentId page:(int)page;

//+ (NSArray *)buildRangesElem:(NSArray *)hitRanges text:(NSString *)text;
- (NSArray*)imageTextSearch:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId query:(NSString*)term;


// ページ情報取得系
- (UIView *)getTileImageWithDocument:(id<NSObject>)metaDocumentId documentId:(id)documentId type:(NSString *)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution;
- (NSArray*)singlePageInfoList:(id<NSObject>)metaDocumentId documentId:(id)docId;
- (UIImage*)thumbnail:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page;
- (NSString*)texts:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page;
- (NSArray*)annotations:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page;
- (NSString *)imageText:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page;
- (NSArray *)regions:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page;


// ページにひもづく情報(廃止)
// これらは、各々のクラスが管理すべき
- (NSArray*)highlights:(id<NSObject>)metaDocumentId page:(int)page;
- (NSArray*)freehand:(id<NSObject>)metaDocumentId page:(int)page;
- (BOOL)saveHighlights:(id<NSObject>)metaDocumentId page:(int)page data:(NSArray *)data;
- (BOOL)saveFreehand:(id<NSObject>)metaDocumentId page:(int)page data:(NSArray *)data;

// アプリケーションレベル ユーザ操作系
- (DocumentContext *)history;
- (BOOL)saveHistory:(DocumentContext *)history;


- (void)startDownload:(id<NSObject>)docId baseUrl:(NSString*)baseUrl;
- (NSString *)getFullPath:(NSString *)fileName;

@end
