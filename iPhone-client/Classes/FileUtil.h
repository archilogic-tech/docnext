//
//  FileUtil.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TOCObject.h"
#import "HistoryObject.h"
#import "DownloadStatusObject.h"

@interface FileUtil : NSObject {
}

+ (NSString *)getFullPath:(NSString *)fileName;
+ (BOOL)write:(NSData *)data toFile:(NSString *)fileName;
+ (NSData *)read:(NSString *)fileName;
+ (BOOL)exists:(NSString *)fileName;
+ (BOOL)delete:(NSString *)fileName;

+ (BOOL)existsDocument:(int)documentId;
+ (int)pages:(int)documentId;
+ (NSString *)title:(int)documentId;
+ (NSString *)publisher:(int)documentId;
+ (NSArray *)tocs:(int)documentId;
+ (TOCObject *)toc:(int)documentId page:(int)page;
+ (HistoryObject *)history;
+ (void)saveHistory:(HistoryObject *)history;
+ (DownloadStatusObject *)downloadStatus;
+ (void)saveDownloadStatus:(DownloadStatusObject *)downloadStatus;
+ (void)deleteDownloadStatus;
+ (BOOL)existsDownloadStatus;
+ (NSArray *)downloadedIds;
+ (void)saveDownloadedIds:(NSArray *)downloadedIds;
+ (void)deleteCache:(int)docId;

@end
