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
#import "BookmarkObject.h"

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

// ドキュメントメタ情報系
- (NSDictionary*)info:(id<NSObject>)metaDocumentId;
- (BOOL)existsDocument:(id<NSObject>)metaDocumentId;
- (int)pages:(id<NSObject>)metaDocumentId;
- (NSString *)title:(id<NSObject>)metaDocumentId;
- (NSString *)publisher:(id<NSObject>)metaDocumentId;


- (NSArray *)tocs:(id<NSObject>)metaDocumentId;

- (int)imageTextSearch:(NSArray**)pages ranges:(NSArray**)ranges; // 全文検索
- (NSArray *)regions:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page;
- (UIView *)getTileImageWithDocument:(id<NSObject>)metaDocumentId documentId:(id)documentId type:(NSString *)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution;

- (UIImage*)thumbnail:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page;
- (NSString*)texts:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page; // imageTextとの違いは?

// ドキュメントレベルユーザ操作系
- (NSArray*)highlights:(id<NSObject>)metaDocumentId page:(int)page;
- (NSArray*)freehand:(id<NSObject>)metaDocumentId page:(int)page;
- (NSArray*)annotations:(id<NSObject>)metaDocumentId page:(int)page;

- (BOOL)saveHighlights:(id<NSObject>)metaDocumentId page:(int)page data:(NSArray *)data;
- (BOOL)saveFreehand:(id<NSObject>)metaDocumentId page:(int)page data:(NSArray *)data;
//- (BOOL)saveAnnotations:(id<NSObject>)metaDocumentId page:(int)page data:(NSArray *)data;

- (void)deleteCache:(id<NSObject>)metaDocumentId;

// アプリケーションレベル ユーザ操作系
- (NSArray *)bookmark;
- (DocumentContext *)history;

- (BOOL)saveBookmark:(NSArray *)bookmark;
- (BOOL)saveHistory:(DocumentContext *)history;


- (void)startDownload:(id)docId baseUrl:(NSString*)baseUrl;
- (NSString *)getFullPath:(NSString *)fileName;

@end
