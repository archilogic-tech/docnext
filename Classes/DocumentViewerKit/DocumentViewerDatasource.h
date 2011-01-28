//
//  DocumentViewerDatasource.h
//  MapDoc
//
//  Created by sakukawa on 10/11/08.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TOCObject.h"
#import "DocumentContext.h"
#import "DownloadStatusObject.h"
#import "DocumentDownloadManager.h"
#import "LocalStorageManager.h"

@protocol DownloadManagerDelegate;


/*!
    @protocol    DocumentViewerDatasource
    @abstract    DocumentViewerのデータソース
    @discussion  テスト
*/
@protocol DocumentViewerDatasource

@property (nonatomic, assign) id<NSObject,DownloadManagerDelegate> downloadManagerDelegate;
@property (nonatomic, readonly) id<LocalStorageManager> localStorageManager;

#if 0



// ダウンロード系
- (void)startDownload:(id<NSObject>)docId baseUrl:(NSString*)baseUrl;

/*
- (BOOL)hasDownloading;
- (BOOL)saveDownloadStatus:(DownloadStatusObject *)downloadStatus;
- (BOOL)deleteDownloadStatus;
- (DownloadStatusObject *)downloadStatus;
*/


/*!
    @method     existsDocument:
    @abstract   引数で指定された文書が存在するかどうか
    @discussion 
    @param      documentId 存在を確認する文書ID
    @result     文書が存在する場合YES
*/
- (BOOL)existsDocument:(id)documentId;


/*!
    @method     pages:
    @abstract   指定された文書IDの文書のページ数を返す
    @discussion 
    @param      documentId 文書ID
    @result     文書のページ数を返す
*/
- (int)pages:(id)documentId;


/*!
    @method     title:
	@abstract   指定された文書IDの文書のタイトルを返す
    @discussion 
    @param      documentId 文書ID
    @result     文書タイトルを返す
*/
- (NSString *)title:(id)documentId;


/*!
    @method     publisher:
	@abstract   指定された文書IDの文書の著者を返す
    @discussion 
	@param      documentId 文書ID
    @result     著者名を返す
*/
- (NSString *)publisher:(id)documentId;


/*!
    @method     ratio:
    @abstract   指定された文書IDの文書の最大倍率(?)を返す
    @discussion 
    @param      documentId 文書ID
    @result     文書の最大倍率?
*/
- (double)ratio:(id<NSObject>)metaDocumentId documentId:(id)documentId;

/*!
    @method     tocs:
    @abstract   文書のTOC一覧を返す
    @discussion 
    @param      documentId 文書ID
    @result     TOC一覧
*/
- (NSArray *)tocs:(id)documentId;


/*!
    @method     toc:page:
    @abstract   指定されたページのTOC一覧を返す
    @discussion 
    @param      documentId 文書ID
    @param      page ページ番号(0から始まる?)
    @result     TOC Object.
*/
- (TOCObject *)toc:(id)documentId page:(int)page;

/*!
    @method     imageText:page:
    @abstract   imageTextを返す?
    @discussion 
    @param      docId 文書ID
    @param      page ページ
    @result     imageText
*/
- (NSString *)imageText:(id)docId page:(int)page;


/*!
    @method     regions:page:
    @abstract   region一覧を返す?
    @discussion 
    @param      docId 文書ID
    @param      page ページ
    @result     ?
*/
- (NSArray *)regions:(id)docId page:(int)page;


/*!
    @method     deleteCache:
    @abstract   ダウンロード済みのキャッシュを削除する
    @discussion 
    @param      docId 削除するダウンロード済み文書のID
*/
- (void)deleteCache:(id<NSObject>)metaDocumentId;

/*!
    @method     getTileImageWithDocument:type:page:column:row:resolution:
    @abstract   指定されたタイル画像を返す
    @discussion 
    @param      documentId 文書ID
    @param      type デバイスタイプ
    @param      page ページ
    @param      column 列
    @param      row 行
    @param      resolution 倍率
    @result     UIViewを返す。ここで返されるオブジェクトがそのまま表示される。
*/
- (UIView*)getTileImageWithDocument:(id)documentId type:(NSString*)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution;
- (NSDictionary*)info:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId;

- (void)didReceiveMemoryWarning;
- (void)updateSystemFromVersion:(NSString*)currentVersion toVersion:(NSString*)newVersion;


// HGMTODO 以下、別の場所に移すので、文書化しない!!


// ユーザ操作系
- (NSArray*)highlights:(id)docId page:(int)page;
- (BOOL)saveHighlights:(id)docId page:(int)page data:(NSArray*)data;

- (NSArray*)annotations:(id)docId page:(int)page;
//- (BOOL)saveAnnotations:(id)docId page:(int)page data:(NSArray*)data;

- (NSArray*)freehand:(id)docId page:(int)page;
- (BOOL)saveFreehand:(id)docId page:(int)page data:(NSArray*)data;

- (NSArray *)downloadedIds;
- (BOOL)saveDownloadedIds:(NSArray *)downloadedIds;

- (DocumentContext *)history;
- (BOOL)saveHistory:(DocumentContext *)history;

//- (NSArray *)bookmark;
//- (BOOL)saveBookmark:(NSArray *)bookmark;

- (UIImage*)thumbnail:(id)docId cover:(int)cover;
- (NSString*)texts:(id)docId page:(int)page;




// temporary
- (NSString*)libraryURL;

#endif

@end
