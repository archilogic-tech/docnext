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
#define LibraryURL (@"http://ustdoc.com/docman_optimage/library.html")
#define ReachabilityHost (@"ustdoc.com")

@class UstDocDownloadManager;

@interface UstDocDatasource : NSObject<DocumentViewerDatasource> {
	NSOperationQueue *_imageFetchQueue;
	UstDocDownloadManager *_downloadManager;
	StandardLocalStorageManager *_localStorage;
}

//@property (nonatomic, assign) id<NSObject,DownloadManagerDelegate> downloadManagerDelegate;

- (NSString *)getFullPath:(NSString *)fileName;

// temporary
- (NSString*)libraryURL;

// ダウンロード系
- (void)startDownload:(id<NSObject>)docId baseUrl:(NSString*)baseUrl;
- (BOOL)hasDownloading;
- (BOOL)saveDownloadStatus:(DownloadStatusObject *)downloadStatus;
- (BOOL)deleteDownloadStatus;
- (DownloadStatusObject *)downloadStatus;

@end
