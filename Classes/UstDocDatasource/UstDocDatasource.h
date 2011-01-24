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

	NSMutableDictionary *_imageCache;
}

- (NSString *)getFullPath:(NSString *)fileName;

@end
