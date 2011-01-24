//
//  UstDocDatasource.m
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import "UstDocDatasource.h"
#import "Region.h"
#import "UIRemoteImageView.h"
#import "TiledScrollView.h"
#import "NSString+Data.h"


@implementation UstDocDatasource


- (id)init
{
	if ((self = [super init]) != nil) {
		// do something...
		_imageFetchQueue = [[NSOperationQueue alloc] init];
		_localStorage = [[StandardLocalStorageManager alloc] init];
        _downloadManager = [[UstDocDownloadManager alloc] init];
		_downloadManager.datasource = self;

		// TODO for debug
		[self deleteDownloadStatus];

        if ( [self hasDownloading] ) {
            [_downloadManager resume];
        }
	}
	return self;
}

- (void)dealloc
{
	[_imageFetchQueue release];
	[_downloadManager release];
	[_localStorage release];
	[super dealloc];
}

- (id<NSObject,DownloadManagerDelegate>)downloadManagerDelegate
{
	return _downloadManager.delegate;
}

- (void)setDownloadManagerDelegate:(id <NSObject,DownloadManagerDelegate>)d
{
	_downloadManager.delegate = d;
}




#pragma mark custom

// 書籍のメタ情報を取得するメソッド

- (NSDictionary*)info:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId {
	return [_localStorage objectWithDocumentId:metaDocumentId documentId:documentId forKey:@"info"];
}
/*
- (BOOL)existsDocument:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId {
	return [_localStorage existsWithDocumentId:documentId forKey:@"info"];
}
*/
- (BOOL)existsDocument:(id<NSObject>)metaDocumentId {
	return [_localStorage existsWithMetaDocumentId:metaDocumentId];
//	return YES;
//	return [_localStorage existsWithDocumentId:documentId forKey:@"info"];
}

- (int)pages:(id<NSObject>)metaDocumentId
{
	int sum = 0;
	for (NSString *did in metaDocumentId) {
		sum += [[[self info:metaDocumentId documentId:did] objectForKey:@"pages"] intValue];
	}
	return sum;
}


- (int)pages:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)documentId
{
	return [[[self info:metaDocumentId documentId:documentId] objectForKey:@"pages"] intValue];
}

- (NSString *)title:(id<NSObject>)metaDocumentId documentId:(id)documentId {
    return [[self info:metaDocumentId documentId:documentId] objectForKey:@"title"];
}

- (NSString *)publisher:(id<NSObject>)metaDocumentId documentId:(id)documentId {
    return [[self info:metaDocumentId documentId:documentId] objectForKey:@"publisher"];
}

- (double)ratio:(id<NSObject>)metaDocumentId documentId:(id)documentId {
    return [[[self info:metaDocumentId documentId:documentId] objectForKey:@"ratio"] doubleValue];
}

- (NSArray *)tocs:(id<NSObject>)metaDocumentId documentId:(id)documentId {
    NSMutableArray *ret = [NSMutableArray arrayWithCapacity:0];
    
	NSDictionary *d = [_localStorage objectWithDocumentId:metaDocumentId documentId:documentId forKey:@"toc"];
    for ( NSDictionary *dic in d) {
        [ret addObject:[TOCObject objectWithDictionary:dic]];
    }
    
    return ret;
}

- (TOCObject *)toc:(id<NSObject>)metaDocumentId documentId:(id)documentId page:(int)page {
    NSArray *tocs = [self tocs:metaDocumentId documentId:documentId];
    
    for ( int index = [tocs count] - 1 ; index >= 0 ; index-- ) {
        TOCObject *toc = [tocs objectAtIndex:index];
        if ( toc.page <= page ) {
            return toc;
        }
    }
    assert(0);
}


- (NSString *)imageText:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page
{
	NSString *key = [NSString stringWithFormat:@"texts/%d.image.txt", page];
	NSData *data = [_localStorage dataWithDocumentId:metaDocumentId documentId:docId forKey:key];
	return [NSString stringWithData:data];
}

- (NSArray *)regions:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page {
    static int SIZEOF_DOUBLE = 8;
    static int N_REGION_FIELDS = 4;
    
    NSMutableArray *ret = [NSMutableArray arrayWithCapacity:0];
    
	NSString *key = [NSString stringWithFormat:@"texts/%d.regions", page];
    NSData *data = [_localStorage dataWithDocumentId:metaDocumentId documentId:docId forKey:key];
	
    int len = data.length;
    for ( int pos = 0 ; pos < len ; pos += SIZEOF_DOUBLE * N_REGION_FIELDS ) {
        Region *region = [[Region new] autorelease];
        
        double value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 0, SIZEOF_DOUBLE)];
        region.x = value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 1, SIZEOF_DOUBLE)];
        region.y = value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 2, SIZEOF_DOUBLE)];
        region.width = value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 3, SIZEOF_DOUBLE)];
        region.height = value;
        
        [ret addObject:region];
    }
    
    return ret;
}




///////////////////////////////////////////////////////////////////////////////

// ダウンロード系

- (DownloadStatusObject *)downloadStatus {
	NSDictionary *d = [_localStorage objectForKey:@"downloadStatus"];
    return [DownloadStatusObject objectWithDictionary:d];
}

- (BOOL)saveDownloadStatus:(DownloadStatusObject *)downloadStatus {
	return [_localStorage saveObject:[downloadStatus toDictionary] forKey:@"downloadStatus"];
}

- (BOOL)deleteDownloadStatus {
	return [_localStorage removeForKey:@"downloadStatus"];
}

- (NSArray *)downloadedIds {
	return [_localStorage objectForKey:@"downloadedIds"];
}

- (BOOL)saveDownloadedIds:(NSArray *)downloadedIds {
	return [_localStorage saveObject:downloadedIds
							  forKey:@"downloadedIds"];
}



// 生ドキュメント固有情報系 ////////////////////////////////////////////////////////////

- (UIView *) getTileImageWithDocument:(id<NSObject>)metaDocumentId documentId:(id)documentId type:(NSString *)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution
{
	NSString *key = [NSString stringWithFormat:@"images/%@-%d-%d-%d-%d.jpg", type, page, resolution, column, row];
	
	if ([_localStorage existsWithDocumentId:documentId forKey:key]) {

		NSData *d = [_localStorage dataWithDocumentId:documentId forKey:key];
		UIImageView *tile = [[[UIImageView alloc] initWithImage:[UIImage imageWithData:d]] autorelease];
		
		// HGMTODO
		// これは指定しないといけないのか?
        tile.tag = TiledScrollViewTileLocal;
        return tile;
    } else {
        UIRemoteImageView *tile = [[[UIRemoteImageView alloc ] initWithFrame:CGRectZero] autorelease];

		NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%@&page=%d&level=%d&px=%d&py=%d" ,
						 ServerEndpoint , (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone") ,
						 documentId , page , resolution , column , row];
		
		// HGMTODO
		[tile load:_imageFetchQueue url:url];
        return tile;
    }
}


- (NSArray*)singlePageInfo:(id<NSObject>)metaDocumentId documentId:(id)docId {
	return [_localStorage objectWithDocumentId:metaDocumentId documentId:docId forKey:@"singlePageInfo"];
}

- (UIImage*)thumbnail:(id<NSObject>)metaDocumentId documentId:(id)docId cover:(int)cover {
	NSString *key = [NSString stringWithFormat:@"images/thumb-%d.jpg", cover];
	NSData *data = [_localStorage dataWithDocumentId:metaDocumentId documentId:docId forKey:key];
	return [UIImage imageWithData:data];
}


- (NSString*)texts:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page {
	NSString *key = [NSString stringWithFormat:@"texts/%d", page];
	NSString *text = [NSString stringWithData:[_localStorage dataWithDocumentId:metaDocumentId documentId:docId forKey:key]];
	return text;
}




// ユーザ操作系ドキュメント関連
- (void)deleteCache:(id<NSObject>)metaDocumentId documentId:(id)docId {
	[_localStorage removeWithDocumentId:metaDocumentId documentId:docId];
	return;
	
	// TODO 細かく消さなくてはならないのか?
	/*
	
	HistoryObject* history = [self history];
//	id<NSObject> did = history.documentContext.documentId;
    if ( [history.documentContext.documentId compare:docId] == NSOrderedSame ) {
		[_localStorage removeForKey:@"history"];
    }
    
    NSMutableArray *bookmarks = [NSMutableArray arrayWithCapacity:0];
	NSDictionary *d = [_localStorage objectForKey:@"bookmarks"];
    for ( NSDictionary *dic in d) {
        BookmarkObject *obj = [BookmarkObject objectWithDictionary:dic];
        if ( [obj.documentContext.documentId compare:docId] != NSOrderedSame ) {
            [bookmarks addObject:dic];
        }
    }
	[_localStorage saveObject:bookmarks forKey:@"bookmarks"];
    
    NSMutableArray *downloadedIds = [NSMutableArray arrayWithArray:[self downloadedIds]];
    for ( id downloadedId in downloadedIds ) {
        if ( [downloadedId compare:docId] == NSOrderedSame ) {
            [downloadedIds removeObject:downloadedId];
            break;
        }
    }
    [self saveDownloadedIds:downloadedIds];
	 */
}

- (NSArray*)highlights:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page
{
	NSString *key = [NSString stringWithFormat:@"%d.highlight", page];
	return [_localStorage objectWithDocumentId:metaDocumentId documentId:docId forKey:key];
}

- (BOOL)saveHighlights:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page data:(NSArray *)data
{
	NSString *key = [NSString stringWithFormat:@"%d.highlight", page];
	return [_localStorage saveObjectWithDocumentId:metaDocumentId documentId:docId object:data forKey:key];
}

- (NSArray*)annotations:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page
{
	NSString *key = [NSString stringWithFormat:@"images/%d.anno", page];
	return [_localStorage objectWithDocumentId:metaDocumentId documentId:docId forKey:key];
}

- (NSArray*)freehand:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page
{
	NSString *key = [NSString stringWithFormat:@"images/%d.freehand", page];
	return [_localStorage objectWithDocumentId:metaDocumentId documentId:docId forKey:key];
}

- (BOOL)saveFreehand:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page data:(NSArray *)data
{
	NSString *key = [NSString stringWithFormat:@"images/%d.freehand", page];
	return [_localStorage saveObjectWithDocumentId:metaDocumentId documentId:docId object:data forKey:key];
}

- (HistoryObject *)history {
	return [HistoryObject objectWithDictionary:[_localStorage objectForKey:@"history"]];
}

- (BOOL)saveHistory:(HistoryObject *)history {
	return [_localStorage saveObject:[history toDictionary] forKey:@"history"];
}

- (NSArray *)bookmark
{
	return [_localStorage objectForKey:@"bookmarks"];
}

- (BOOL)saveBookmark:(NSArray *)bookmark
{
	return [_localStorage saveObject:bookmark forKey:@"bookmarks"];
}

- (NSString*)libraryURL
{
	return LibraryURL;
}

- (void)startDownload:(id)docId baseUrl:(NSString*)baseUrl
{
	[_downloadManager startMetaInfoDownload:docId baseUrl:baseUrl];
}

- (BOOL)hasDownloading
{
	return [_localStorage existsForKey:@"downloadStatus"];
}


- (NSString *)getFullPath:(NSString *)fileName
{
	return [_localStorage getFullPath:fileName];
}





@end
