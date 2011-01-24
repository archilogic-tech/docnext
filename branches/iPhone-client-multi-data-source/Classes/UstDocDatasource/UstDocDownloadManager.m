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

@interface UstDocDownloadManager ()
- (void)downloadNextPage:(id)docId page:(int)page px:(int)px py:(int)py;
- (void)didMetaInfoDownloadFinished:(ASIHTTPRequest *)request;
@end

@implementation UstDocDownloadManager

@synthesize delegate;
@synthesize datasource = _datasource;

- (void)startMetaInfoDownload:(id)docId baseUrl:(NSString*)baseUrl
{
	[self startMetaInfoDownload:docId baseUrl:baseUrl index:0];
}


- (void)startMetaInfoDownload:(id)metaDocumentId baseUrl:(NSString*)baseUrl index:(int)idx
{
	int currentDownloadIndex = idx;
	
//	NSString *did = nil;
//	if ([metaDocumentId isKindOfClass:[NSArray class]]) {
	NSString *did = [metaDocumentId objectAtIndex:currentDownloadIndex];
/*
	} else {
		did = docId;
	}
*/	
	
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
}

- (void)resume {
    DownloadStatusObject *downloadStatus = [_datasource downloadStatus];
    
    [self downloadNextPage:downloadStatus.metaDocumentId
				documentId:downloadStatus.docId
					  page:downloadStatus.downloadedPage
						px:downloadStatus.downloadedPx
                        py:downloadStatus.downloadedPy];
}

- (void)downloadPage:(id<NSObject>)metaDocumentId documentId:(id)docId page:(int)page px:(int)px py:(int)py
{
	NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    int level = [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
    NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%@&page=%d&level=%d&px=%d&py=%d" ,
                     ServerEndpoint , type , docId , page , level , px , py];
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    request.delegate = self;
    request.didFinishSelector = @selector(didPageDownloadFinished:);
	request.didFailSelector = @selector(didPageDownloadFailed:);
	
	// 直接正しいところに書き込む
	NSString *dest = [NSString stringWithFormat:@"%@/%@/images/%@-%d-%d-%d-%d.jpg" , [(NSArray*)metaDocumentId componentsJoinedByString:@","] , docId , type , page , level , px , py];
	request.downloadDestinationPath = [_datasource getFullPath:dest];
	
    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
    [request.userInfo setValue:metaDocumentId forKey:@"metaDocumentId"];
    [request.userInfo setValue:docId forKey:@"documentId"];
    [request.userInfo setValue:[NSNumber numberWithInt:page] forKey:@"page"];
    [request.userInfo setValue:[NSNumber numberWithInt:px] forKey:@"px"];
    [request.userInfo setValue:[NSNumber numberWithInt:py] forKey:@"py"];
    
    [request startAsynchronous];
}

- (void)downloadComplete:(id)docId {
    if (delegate &&  [delegate respondsToSelector:@selector(didAllPagesDownloadFinished:)] ) {
        [delegate didAllPagesDownloadFinished:docId];
    }
    [_datasource deleteDownloadStatus];
    
    NSMutableArray *downloaded = [NSMutableArray arrayWithArray:[_datasource downloadedIds]];
    [downloaded addObject:[NSString stringWithFormat:@"%@" , docId]];
    [_datasource saveDownloadedIds:downloaded];
}

- (void)downloadNextPage:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId page:(int)page px:(int)px py:(int)py {
    int pages = [_datasource pages:metaDocumentId];
    
	// ダウンロードの進捗を伝える
	// TODO NSNotificationに変更する
/*	
	if ( [delegate respondsToSelector:@selector(pageDownloadProgressed:downloaded:)] ) {
		[delegate pageDownloadProgressed:docId downloaded:(1.0 * ( page + 1.0 ) / pages)];
	}
*/	
    if ( [[UIScreen mainScreen] scale] == 2.0 ) {
        if ( px < 1 ) {
            [self downloadPage:docId page:page px:(px + 1) py:py];
        } else if ( py < 1 ) {
            [self downloadPage:docId page:page px:0 py:(py + 1)];
        } else if ( page + 1 < pages ) {
            [self downloadPage:docId page:(page + 1) px:0 py:0];
        } else {
            [self downloadComplete:docId];
        }
    } else {
        if ( page + 1 < pages ) {
            [self downloadPage:metaDocumentId documentId:docId page:(page + 1) px:0 py:0];
        } else {
            [self downloadComplete:docId];
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

#pragma mark ASIHTTPRequest didFinishSelector

- (void)didMetaInfoDownloadFailed:(ASIHTTPRequest *)request
{
	id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
    id<NSObject> docId = [request.userInfo objectForKey:@"documentId"];
	int currentDownloadIndex = [[request.userInfo objectForKey:@"currentDownloadIndex"] intValue];

//	NSString *did = docId;
/*
	if ([docId isKindOfClass:[NSArray class]]) {
		did = [(NSArray*)docId objectAtIndex:currentDownloadIndex];
	} else {
		did = (NSString*)docId;
	}
*/	
	
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

	dirName = [_datasource getFullPath:dirName];
	[[NSFileManager defaultManager] createDirectoryAtPath:dirName withIntermediateDirectories:YES attributes:nil error:nil];
	

	////////// zip処理 /////////////////////////////
	
    NSString *zipName = [request.userInfo objectForKey:@"tempFileName"];
	
	// メタ情報を展開してローカルのストレージにキャッシュする
	[_datasource deleteCache:metaDocumentId documentId:docId];
    ZipArchive *zip = [[ZipArchive new] autorelease];
    if ( [zip UnzipOpenFile:zipName] ) {
        [zip UnzipFileTo:dirName overWrite:YES];
        [zip UnzipCloseFile];
    } else {
		// error
	}

	[[NSFileManager defaultManager] removeItemAtPath:zipName error:nil];
	
	// Take care this way is depended on the fact 2x2 is max
	// TODO 意味を理解して修正すること
	[self updateDownloadStatus:metaDocumentId documentId:docId page:-1 px:1 py:1];
	
	// 誌面画像のダウンロードを別スレッドで開始する
    [self downloadPage:metaDocumentId documentId:docId page:0 px:0 py:0];
    
	currentDownloadIndex++;
	if ([metaDocumentId isKindOfClass:[NSArray class]]) {
		int count = [(NSArray*)metaDocumentId count];
		if (currentDownloadIndex < count) {
			// まだ全META情報のダウンロードが完了していないので次を開始行う
			NSString *baseUrl = [request.userInfo objectForKey:@"baseUrl"];
			[self startMetaInfoDownload:metaDocumentId baseUrl:baseUrl index:currentDownloadIndex];
			return;

		}
//		did = [(NSArray*)docId objectAtIndex:currentDownloadIndex];
	}

    if (delegate && [delegate respondsToSelector:@selector(didMetaInfoDownloadFinished:)] ) {
        [delegate didMetaInfoDownloadFinished:metaDocumentId];
    }
}

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
    id docId = [request.userInfo objectForKey:@"documentId"];
	
	// TODO きちんとエラー情報を渡すこと
	// NSNotifacationにする
	/*
	if ( [delegate respondsToSelector:@selector(didPageDownloadFailed:error:)] ) {
		[delegate didPageDownloadFailed:docId error:[request error]];
	}
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
	 */
}


#pragma mark ASIHTTPRequestDelegate

- (void)requestFailed:(ASIHTTPRequest *)request
{
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}



@end
