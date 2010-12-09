//
//  DownloadManagerDelegate.h
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@protocol DownloadManagerDelegate


@optional

/*!
    @method     didMetaInfoDownloadFinished:
    @abstract   メタ情報のダウンロードが終了した時に呼ばれる
    @discussion 
    @param      docId 文書ID
*/
- (void)didMetaInfoDownloadFinished:(id)docId;


/*!
    @method     didMetaInfoDownloadFailed:error:
    @abstract   メタ情報のダウンロードに失敗した場合
    @discussion 
    @param      docId ドキュメントID
    @param      error エラー情報
*/
- (void)didMetaInfoDownloadFailed:(id)docId error:(NSError*)error;

/*!
    @method     pageDownloadProgressed:downloaded:
    @abstract   誌面画像のダウンロード率(0〜1.0)?を返す
    @discussion <#(comprehensive description)#>
    @param      docId 文書ID
    @param      downloaded 
*/
- (void)pageDownloadProgressed:(id)docId downloaded:(float)downloaded;

/*!
    @method     didAllPagesDownloadFinished:
    @abstract   誌面画像ダウンロード完了した
    @discussion 
    @param      docId 文書ID
*/
- (void)didAllPagesDownloadFinished:(id)docId;

/*!
    @method     didPageDownloadFailed:error:
    @abstract   誌面画像エラーが発生した
    @discussion 
    @param      docId ドキュメントID
    @param      error エラー情報
*/
- (void)didPageDownloadFailed:(id)docId error:(NSError*)error;

@end
