//
//  UstDocDownloadManager.m
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import "UstDocDownloadManager.h"
#import "DocumentViewerConst.h"
#import "ASIHTTPRequest.h"
#import "ZipArchive.h"
#import "UstDocDatasource.h"

#import "PageDownloadOperation.h"

@interface UstDocDownloadManager ()
- (void)downloadNextPage:(id)docId page:(int)page px:(int)px py:(int)py;
- (void)didMetaInfoDownloadFinished:(ASIHTTPRequest *)request;
@end

@implementation UstDocDownloadManager

@synthesize delegate;
@synthesize datasource = _datasource;

- (id) init
{
	self = [super init];
	if (self) {
		_pageDownloadQueue = [[NSOperationQueue alloc] init];
	}
	return self;
}

- (void)dealloc
{
	[_datasource release];
	[_pageDownloadQueue release];
	[super dealloc];
}



- (void)startMetaInfoDownload:(id<NSObject>)metaDocumentId baseUrl:(NSString*)baseUrl
{
	[self startMetaInfoDownload:metaDocumentId baseUrl:baseUrl index:0];
}


- (void)startMetaInfoDownload:(id<NSObject>)metaDocumentId baseUrl:(NSString*)baseUrl index:(int)idx
{
	int currentDownloadIndex = idx;
	
	NSString *did = [metaDocumentId objectAtIndex:currentDownloadIndex];
	
	NSString *url = [NSString stringWithFormat:@"%@download?documentId=%@" , ((UstDocDatasource*)_datasource).serverEndPoint , did];
	NSLog(@"%@ downloading...", url);
    
	ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    request.delegate = self;
    request.didFinishSelector = @selector(didMetaInfoDownloadFinished:);
	request.didFailSelector = @selector(didMetaInfoDownloadFailed:);

	// TODO
	NSString *tempFileName = [NSString stringWithFormat:@"%@/tmpXXXXXXXXXXXXXXXX", NSTemporaryDirectory()];
	char *tmp = strdup([tempFileName cStringUsingEncoding:NSUTF8StringEncoding]);
	mkstemp(tmp);
	tempFileName = [NSString stringWithUTF8String:tmp];
	free(tmp);

	NSLog(@"tempFileName : %@", tempFileName);
	request.downloadDestinationPath = tempFileName;

    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
	[request.userInfo setValue:metaDocumentId forKey:@"metaDocumentId"];
	[request.userInfo setValue:did forKey:@"documentId"];
	[request.userInfo setValue:[NSNumber numberWithInt:currentDownloadIndex] forKey:@"currentDownloadIndex"];
	[request.userInfo setValue:baseUrl forKey:@"baseUrl"];
	[request.userInfo setValue:tempFileName forKey:@"tempFileName"];
    [request startAsynchronous];

	[delegate didMetaInfoDownloadStarted:metaDocumentId];
}

- (void)resume {
	// TODO
	/*
    DownloadStatusObject *downloadStatus = [_datasource downloadStatus];


    [self downloadNextPage:downloadStatus.metaDocumentId
				documentId:downloadStatus.docId
					  page:downloadStatus.downloadedPage
						px:downloadStatus.downloadedPx
                        py:downloadStatus.downloadedPy];
	[delegate didPageDownloadStarted:downloadStatus.metaDocumentId];
 */
}

- (void)downloadPage:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page px:(int)px py:(int)py level:(int)level
{
	NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%@&page=%d&level=%d&px=%d&py=%d" ,
                     ((UstDocDatasource*)_datasource).serverEndPoint , type , docId , page , level , px , py];

	NSString *dest = [NSString stringWithFormat:@"%@/%@/images/%@-%d-%d-%d-%d.jpg" , [(NSArray*)metaDocumentId componentsJoinedByString:@","] , docId , type , page , level , px , py];

	PageDownloadOperation *op = [[PageDownloadOperation alloc] init];
	op.url = url;
	op.destination = [_datasource getFullPath:dest];
	[_pageDownloadQueue addOperation:op];
	[op release];
}

- (void)didDocumentDownloadCompleted:(id<NSObject>)metaDocumentId {

    NSString *mdid = [(NSArray*)metaDocumentId componentsJoinedByString:@","];
	if (delegate && [delegate respondsToSelector:@selector(didAllPagesDownloadFinished:)] ) {
        [delegate didAllPagesDownloadFinished:mdid];
    }
    [_datasource deleteDownloadStatus];

    NSMutableArray *downloaded = [NSMutableArray arrayWithArray:[_datasource downloadedIds]];
    [downloaded addObject:[NSString stringWithFormat:@"%@" , mdid]];
    [_datasource saveDownloadedIds:downloaded];
}

#pragma mark ASIHTTPRequest didFinishSelector

- (void)didMetaInfoDownloadFailed:(ASIHTTPRequest *)request
{
	id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
    id<NSObject> docId = [request.userInfo objectForKey:@"documentId"];
	int currentDownloadIndex = [[request.userInfo objectForKey:@"currentDownloadIndex"] intValue];

	// TODO きちんとエラー情報を渡すこと
	if (delegate && [delegate respondsToSelector:@selector(didMetaInfoDownloadFailed:error:)] ) {
		[delegate didMetaInfoDownloadFailed:docId error:[request error]];
	}
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}

- (void)didMetaInfoDownloadFinished:(ASIHTTPRequest *)request
{
    id<NSObject> docId = [request.userInfo objectForKey:@"documentId"];
    id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
	int currentDownloadIndex = [[request.userInfo objectForKey:@"currentDownloadIndex"] intValue];

	NSString *dirName = [NSString stringWithFormat:@"%@/%@/" ,
						 [(NSArray*)metaDocumentId componentsJoinedByString:@","],
						 docId];

	////////// zip処理 /////////////////////////////
    NSString *zipName = [request.userInfo objectForKey:@"tempFileName"];

	// メタ情報を展開してローカルのストレージにキャッシュする
	// 毎回全部消してしまうのはまずい
	//[_datasource deleteCache:metaDocumentId];

	dirName = [_datasource getFullPath:dirName];
	[[NSFileManager defaultManager] createDirectoryAtPath:dirName withIntermediateDirectories:YES attributes:nil error:nil];
	
    ZipArchive *zip = [[ZipArchive new] autorelease];
    if ( [zip UnzipOpenFile:zipName] ) {
        [zip UnzipFileTo:dirName overWrite:YES];
        [zip UnzipCloseFile];
    } else {
		// error
		assert(0);
	}

	[[NSFileManager defaultManager] removeItemAtPath:zipName error:nil];

	currentDownloadIndex++;
	if ([metaDocumentId isKindOfClass:[NSArray class]]) {
		int count = [(NSArray*)metaDocumentId count];
		if (currentDownloadIndex < count) {
			// まだ全META情報のダウンロードが完了していないので次を開始行う
			NSString *baseUrl = [request.userInfo objectForKey:@"baseUrl"];
			[self startMetaInfoDownload:metaDocumentId baseUrl:baseUrl index:currentDownloadIndex];
			return;
		}
	}

	// TODO 本当はこの時点で呼んではいけない
	[self didDocumentDownloadCompleted:metaDocumentId];

	
	// すべてのメタ情報のダウンロードが完了したら誌面画像のダウンロードを別スレッドで開始する
	[self downloadPageStart:metaDocumentId];

    if (delegate && [delegate respondsToSelector:@selector(didMetaInfoDownloadFinished:)] ) {
        [delegate didMetaInfoDownloadFinished:metaDocumentId];
    }
}

// 誌面画像のURLと保存先をqueueに入れる
- (void)downloadPageStart:(id<NSObject>)metaDocumentId
{
	for (id<NSObject> documentId in metaDocumentId) {

		int pages = [_datasource pages:metaDocumentId documentId:documentId];

		// 全レベルの誌面画像をダウンロードしておく。
		int level = [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
		for (; level < 3; level++) {
			int max = 1 << level;
			for (int p = 0; p < pages; p++) {
				for (int px = 0; px < max; px++) {
					for (int py = 0; py < max; py++) {
//						NSLog(@"%d %d %d %d", p, px, py, level);
						[self downloadPage:metaDocumentId documentId:documentId page:p px:px py:py level:level];
					}
				}
			}
		}		
	}
}

/*
- (void)didPageDownloadFinished:(ASIHTTPRequest *)request {
    id<NSObject> docId = [request.userInfo objectForKey:@"documentId"];
	id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
	int page = [[request.userInfo objectForKey:@"page"] intValue];
    int px = [[request.userInfo objectForKey:@"px"] intValue];
    int py = [[request.userInfo objectForKey:@"py"] intValue];

    [self updateDownloadStatus:metaDocumentId documentId:docId page:page px:px py:py];
    [self downloadNextPage:metaDocumentId documentId:docId page:page px:px py:py];
}

- (void)didPageDownloadFailed:(ASIHTTPRequest *)request
{
	id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
	[delegate didPageDownloadFailed:metaDocumentId error:request.error];
}
 */


#pragma mark ASIHTTPRequestDelegate

- (void)requestFailed:(ASIHTTPRequest *)request
{
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}





@end
