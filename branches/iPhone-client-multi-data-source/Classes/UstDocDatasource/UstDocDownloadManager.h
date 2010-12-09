//
//  UstDocDownloadManager.h
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UstDocDatasource.h"
#import "DownloadManagerDelegate.h"

@class UstDocDatasource;

@interface UstDocDownloadManager : NSObject {
    id<NSObject,DownloadManagerDelegate> delegate;
	UstDocDatasource *_datasource;

}

@property(nonatomic,assign) id<NSObject,DownloadManagerDelegate> delegate;
@property(nonatomic,retain) UstDocDatasource *datasource;

/*!
    @method     startMetaInfoDownload:baseUrl:
    @abstract   文書メタ情報のダウンロードを開始する
    @discussion 
    @param      docId 文書ID
    @param      baseUrl ベースURL
*/
- (void)startMetaInfoDownload:(id)docId baseUrl:(NSString*)baseUrl;

/*!
    @method     resume
    @abstract   誌面画像ダウンロードを再開させる
    @discussion <#(comprehensive description)#>
*/
- (void)resume;

@end
