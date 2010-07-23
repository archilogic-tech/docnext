//
//  DownloadUtil.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol DownloadManagerDelegate

@optional
- (void)initDownloadCompleted:(int)docId;
- (void)downloadProgressed:(int)docId downloaded:(float)downloaded;
- (void)downloadCompleted:(int)docId;

@end

@interface DownloadManager : NSObject {
    id<NSObject,DownloadManagerDelegate> delegate;
}

@property(nonatomic,assign) id<NSObject,DownloadManagerDelegate> delegate;

+ (DownloadManager *)instance;

- (void)startDownload:(int)docId;
- (BOOL)hasDownloading;
- (void)resume;

@end
