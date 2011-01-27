//
//  UstDocDownloadManager.h
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UstDocDatasource.h"
#import "DocumentDownloadManager.h"


@class UstDocDatasource;

@interface UstDocDownloadManager : NSObject<DocumentDownloadManager> {
    id<NSObject,DownloadManagerDelegate> delegate;
	id<NSObject,DocumentViewerDatasource> _datasource;

	NSOperationQueue *_pageDownloadQueue;
}


@property(nonatomic,assign) id<NSObject,DownloadManagerDelegate> delegate;
@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;

- (void)startMetaInfoDownload:(id<NSObject>)docId baseUrl:(NSString*)baseUrl;

- (void)startMetaInfoDownload:(id<NSObject>)docId baseUrl:(NSString*)baseUrl index:(int)idx;


/*!
    @method     resume
    @abstract   誌面画像ダウンロードを再開させる
    @discussion <#(comprehensive description)#>
*/
- (void)resume;

@end
