//
//  SampleDatasource.h
//  MapDoc
//
//  Created by sakukawa on 10/11/09.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DocumentViewerDatasource.h"
#import "MediaDoDownloadManager.h"
#import "StandardLocalStorageManager.h"
#import "DownloadManagerDelegate.h"

@class MediaDoDownloadManager;

@interface SampleDatasource : NSObject<DocumentViewerDatasource> {
	NSOperationQueue *_imageFetchQueue;
	MediaDoDownloadManager *_downloadManager;
	StandardLocalStorageManager *_localStorage;
}

@property (nonatomic, assign) id<NSObject,DownloadManagerDelegate> downloadManagerDelegate;


// 文書のメタ情報取得系
- (BOOL)existsDocument:(id)documentId;

- (int)pages:(id<NSObject>)documentId;
- (NSString *)title:(id<NSObject>)documentId;
- (NSString *)publisher:(id<NSObject>)documentId;
- (double)ratio:(id<NSObject>)documentId;
- (NSArray *)tocs:(id<NSObject>)documentId;

- (TOCObject *)toc:(id<NSObject>)documentId page:(int)page;
- (NSString *)imageText:(id<NSObject>)docId page:(int)page;
- (NSArray *)regions:(id<NSObject>)docId page:(int)page;
- (NSArray*)singlePageInfo:(id<NSObject>)docId;
	
- (UIImage*)thumbnail:(id<NSObject>)docId cover:(int)cover;
- (NSString*)texts:(id<NSObject>)docId page:(int)page;


// local storage系 ///////////////////////////////////////////////////////

// MediaDoのベースURLを保存する
- (BOOL)saveBaseUrl:(NSString*)s documentId:(id<NSObject>)docId;
- (NSString*)baseUrl:(id<NSObject>)docId;

- (NSArray *)downloadedIds;
- (BOOL)saveDownloadedIds:(NSArray *)downloadedIds;
	
- (HistoryObject *)history;
- (BOOL)saveHistory:(HistoryObject *)history;

- (NSArray *)bookmark;
- (BOOL)saveBookmark:(NSArray *)bookmark;

// local storage 文書系 ////////////////////////////////////////////////

// ローカルキャッシュ操作
- (void)deleteCache:(id<NSObject>)docId;

// ユーザ操作系情報保存
- (NSArray*)highlights:(id<NSObject>)docId page:(int)page;
- (BOOL)saveHighlights:(id<NSObject>)docId page:(int)page data:(NSArray*)data;

- (NSArray*)annotations:(id<NSObject>)docId page:(int)page;
//- (BOOL)saveAnnotations:(id<NSObject>)docId page:(int)page data:(NSArray*)data;

- (NSArray*)freehand:(id<NSObject>)docId page:(int)page;
- (BOOL)saveFreehand:(id<NSObject>)docId page:(int)page data:(NSArray*)data;


// temporary
- (NSString*)libraryURL;

// このメソッドは推奨されない
- (NSString *)getFullPath:(NSString *)fileName;

// ダウンロード系
- (void)startDownload:(id<NSObject>)docId baseUrl:(NSString*)baseUrl;
- (BOOL)hasDownloading;
- (BOOL)saveDownloadStatus:(DownloadStatusObject *)downloadStatus;
- (BOOL)deleteDownloadStatus;
- (DownloadStatusObject *)downloadStatus;



@end
