//
//  DocumentDownloadManager.h
//  MapDoc
//
//  Created by sakukawa on 11/01/18.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentViewerDatasource.h"

@protocol DocumentViewerDatasource;



@protocol DownloadManagerDelegate


- (void)didMetaInfoDownloadStarted:(id<NSObject>)metaDocumentId;

/*!
 @method     didMetaInfoDownloadFinished:
 @abstract   メタ情報のダウンロードが終了した時に呼ばれる
 @discussion 
 @param      docId 文書ID
 */
- (void)didMetaInfoDownloadFinished:(id<NSObject>)metaDocumentId;


/*!
 @method     didMetaInfoDownloadFailed:error:
 @abstract   メタ情報のダウンロードに失敗した場合
 @discussion 
 @param      docId ドキュメントID
 @param      error エラー情報
 */
- (void)didMetaInfoDownloadFailed:(id<NSObject>)metaDocumentId error:(NSError*)error;


- (void)didPageDownloadStarted:(id<NSObject>)metaDocumentId;


/*!
 @method     pageDownloadProgressed:downloaded:
 @abstract   誌面画像のダウンロード率(0〜1.0)?を返す
 @discussion <#(comprehensive description)#>
 @param      docId 文書ID
 @param      downloaded 
 */
- (void)pageDownloadProgressed:(id<NSObject>)metaDocumentId downloaded:(float)downloaded;

/*!
 @method     didAllPagesDownloadFinished:
 @abstract   誌面画像ダウンロード完了した
 @discussion 
 @param      docId 文書ID
 */
- (void)didAllPagesDownloadFinished:(id<NSObject>)metaDocumentId;

/*!
 @method     didPageDownloadFailed:error:
 @abstract   誌面画像エラーが発生した
 @discussion 
 @param      docId ドキュメントID
 @param      error エラー情報
 */
- (void)didPageDownloadFailed:(id<NSObject>)metaDocumentId error:(NSError*)error;

@end



@protocol DocumentDownloadManager

@property(nonatomic,assign) id<NSObject,DownloadManagerDelegate> delegate;
@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> *datasource;

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
