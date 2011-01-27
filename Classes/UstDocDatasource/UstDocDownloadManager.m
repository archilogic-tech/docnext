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
	
	NSString *url = [NSString stringWithFormat:@"%@download?documentId=%@" , ServerEndpoint , did];
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

- (void)downloadPage:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page px:(int)px py:(int)py
{
	NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    int level = [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
    NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%@&page=%d&level=%d&px=%d&py=%d" ,
                     ServerEndpoint , type , docId , page , level , px , py];

	NSString *dest = [NSString stringWithFormat:@"%@/%@/images/%@-%d-%d-%d-%d.jpg" , [(NSArray*)metaDocumentId componentsJoinedByString:@","] , docId , type , page , level , px , py];

	PageDownloadOperation *op = [[PageDownloadOperation alloc] init];
	op.url = url;
	op.destination = [_datasource getFullPath:dest];
	[_pageDownloadQueue addOperation:op];
	[op release];
}

- (void)downloadComplete:(id<NSObject>)metaDocumentId {

    NSString *mdid = [(NSArray*)metaDocumentId componentsJoinedByString:@","];
	if (delegate && [delegate respondsToSelector:@selector(didAllPagesDownloadFinished:)] ) {
        [delegate didAllPagesDownloadFinished:mdid];
    }
    [_datasource deleteDownloadStatus];

    NSMutableArray *downloaded = [NSMutableArray arrayWithArray:[_datasource downloadedIds]];
    [downloaded addObject:[NSString stringWithFormat:@"%@" , mdid]];
    [_datasource saveDownloadedIds:downloaded];
}
/*
- (void)downloadNextPage:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page px:(int)px py:(int)py {
    int pages = [_datasource pages:metaDocumentId];
    
	// ダウンロードの進捗を伝える
	// TODO NSNotificationに変更しようと思ったが、これで十分そう
	if ( [delegate respondsToSelector:@selector(pageDownloadProgressed:downloaded:)] ) {
		float val = (1.0 * ( page + 1.0 ) / pages);
		[delegate pageDownloadProgressed:docId downloaded:val];
	}
	
    if ( [[UIScreen mainScreen] scale] == 2.0 ) {
        if ( px < 1 ) {
            [self downloadPage:docId page:page px:(px + 1) py:py];
        } else if ( py < 1 ) {
            [self downloadPage:docId page:page px:0 py:(py + 1)];
        } else if ( page + 1 < pages ) {
            [self downloadPage:docId page:(page + 1) px:0 py:0];
        } else {
			int index = [(NSArray*)metaDocumentId indexOfObject:docId];
			if (index == [metaDocumentId count]-1) {
				[self downloadComplete:metaDocumentId];
			}
        }
    } else {
        if ( page + 1 < pages ) {
            [self downloadPage:metaDocumentId documentId:docId page:(page + 1) px:0 py:0];
        } else {
			int index = [(NSArray*)metaDocumentId indexOfObject:docId];
			if (index == [metaDocumentId count]-1) {
				[self downloadComplete:metaDocumentId];
			}
        }
    }
}

- (void)updateDownloadStatus:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page px:(int)px py:(int)py {
    DownloadStatusObject *downloadStatus = [[DownloadStatusObject alloc] init];
    downloadStatus.docId = docId;
	downloadStatus.metaDocumentId = metaDocumentId;
    downloadStatus.downloadedPage = page;
    downloadStatus.downloadedPx = px;
    downloadStatus.downloadedPy = py;

    [_datasource saveDownloadStatus:downloadStatus];
	[downloadStatus release];
//	assert(0);
}
 */

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
	}

	[[NSFileManager defaultManager] removeItemAtPath:zipName error:nil];

	// Take care this way is depended on the fact 2x2 is max
	//	[self updateDownloadStatus:metaDocumentId documentId:docId page:-1 px:1 py:1];

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
	[self downloadComplete:metaDocumentId];

	
	// すべてのメタ情報のダウンロードが完了したら誌面画像のダウンロードを別スレッドで開始する
	[self didDownloadPageStart:metaDocumentId];

    if (delegate && [delegate respondsToSelector:@selector(didMetaInfoDownloadFinished:)] ) {
        [delegate didMetaInfoDownloadFinished:metaDocumentId];
    }
}

- (void)didDownloadPageStart:(id<NSObject>)metaDocumentId
{
	for (id<NSObject> documentId in metaDocumentId) {
		int page = 0;
		int px = 0;
		int py = 0;
		[self downloadPage:metaDocumentId documentId:documentId page:page px:px py:py];

		int pages = [_datasource pages:metaDocumentId documentId:documentId];
		while(true) {
			if ( [[UIScreen mainScreen] scale] == 2.0 ) {
				if ( px < 1 ) {
					[self downloadPage:metaDocumentId documentId:documentId page:page px:++px py:py];
				} else if ( py < 1 ) {
					[self downloadPage:metaDocumentId documentId:documentId page:page px:0 py:++py];
				} else if ( page + 1 < pages ) {
					[self downloadPage:metaDocumentId documentId:documentId page:++page px:0 py:0];
				} else {
					break;
				}
			} else {
				if ( page + 1 < pages ) {
					[self downloadPage:metaDocumentId documentId:documentId page:++page px:0 py:0];
				} else {
					break;
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
