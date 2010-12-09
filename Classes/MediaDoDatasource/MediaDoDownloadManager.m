//
//  DownloadUtil.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "MediaDoDownloadManager.h"
#import "DocumentViewerConst.h"
#import "ASIHTTPRequest.h"
#import "ZipArchive.h"

@interface MediaDoDownloadManager ()
- (void)downloadNextPage:(id)docId page:(int)page px:(int)px py:(int)py;
- (void)didMetaInfoDownloadFinished:(ASIHTTPRequest *)request;
@end


@implementation MediaDoDownloadManager

@synthesize delegate;
@synthesize datasource = _datasource;

- (void)startMetaInfoDownload:(id)docId baseUrl:(NSString*)baseUrl
{
    NSString *url = [NSString stringWithFormat:@"%@/docinfo.zip" , baseUrl , docId];
	NSLog(@"%@ downloading...", url);

	ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
	request.requestHeaders = [NSMutableDictionary dictionaryWithObjectsAndKeys:@"docnext", @"User-Agent", nil];
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
	[request.userInfo setValue:docId forKey:@"id"];
	[request.userInfo setValue:baseUrl forKey:@"baseUrl"];
	[request.userInfo setValue:tempFileName forKey:@"tempFileName"];
    [request startAsynchronous];
}


- (void)resume {
    DownloadStatusObject *downloadStatus = [_datasource downloadStatus];
    
    [self downloadNextPage:downloadStatus.docId page:downloadStatus.downloadedPage px:downloadStatus.downloadedPx
                        py:downloadStatus.downloadedPy];
}

- (void)downloadPage:(id)docId page:(int)page px:(int)px py:(int)py {
    NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    int level = [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
    NSString *url = [NSString stringWithFormat:@"%@/images/%@%d-%d-%d-%d.jpg" ,
                     [_datasource baseUrl:docId] , type , page , level , px , py];
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
	request.requestHeaders = [NSMutableDictionary dictionaryWithObjectsAndKeys:@"docnext", @"User-Agent", nil];
    request.delegate = self;
    request.didFinishSelector = @selector(didPageDownloadFinished:);
	request.didFailSelector = @selector(didPageDownloadFailed:);

	// 直接正しいところに書き込む
	request.downloadDestinationPath = [_datasource getFullPath:[NSString stringWithFormat:@"%@/images/%@-%d-%d-%d-%d.jpg" , docId , type , page , level , px , py]];
	
    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
    [request.userInfo setValue:docId forKey:@"id"];
    [request.userInfo setValue:[NSNumber numberWithInt:page] forKey:@"page"];
    [request.userInfo setValue:[NSNumber numberWithInt:px] forKey:@"px"];
    [request.userInfo setValue:[NSNumber numberWithInt:py] forKey:@"py"];
    
    [request startAsynchronous];
}

- (void)downloadComplete:(id)docId {
    if ( [delegate respondsToSelector:@selector(didAllPagesDownloadFinished:)] ) {
        [delegate didAllPagesDownloadFinished:docId];
    }
    [_datasource deleteDownloadStatus];
    
    NSMutableArray *downloaded = [NSMutableArray arrayWithArray:[_datasource downloadedIds]];
    [downloaded addObject:[NSString stringWithFormat:@"%@" , docId]];
    [_datasource saveDownloadedIds:downloaded];
}

- (void)downloadNextPage:(id)docId page:(int)page px:(int)px py:(int)py {
    int pages = [_datasource pages:docId];
    
	// ダウンロードの進捗を伝える
	if ( [delegate respondsToSelector:@selector(pageDownloadProgressed:downloaded:)] ) {
		[delegate pageDownloadProgressed:docId downloaded:(1.0 * ( page + 1.0 ) / pages)];
	}
	
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
            [self downloadPage:docId page:(page + 1) px:0 py:0];
        } else {
            [self downloadComplete:docId];
        }
    }
}

- (void)updateDownloadStatus:(id)docId page:(int)page px:(int)px py:(int)py {
    DownloadStatusObject *downloadStatus = [[DownloadStatusObject new] autorelease];
    downloadStatus.docId = docId;
    downloadStatus.downloadedPage = page;
    downloadStatus.downloadedPx = px;
    downloadStatus.downloadedPy = py;
    
    [_datasource saveDownloadStatus:downloadStatus];
}

#pragma mark ASIHTTPRequest didFinishSelector

- (void)didMetaInfoDownloadFailed:(ASIHTTPRequest *)request
{
    id docId = [request.userInfo objectForKey:@"id"];

	// TODO きちんとエラー情報を渡すこと
	if ( [delegate respondsToSelector:@selector(didMetaInfoDownloadFailed:error:)] ) {
		[delegate didMetaInfoDownloadFailed:docId error:[request error]];
	}
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}

- (void)didMetaInfoDownloadFinished:(ASIHTTPRequest *)request {

	id docId = [request.userInfo objectForKey:@"id"];
    
    NSString *zipName = [request.userInfo objectForKey:@"tempFileName"];
	
	// メタ情報を展開してローカルのストレージにキャッシュする
    NSString *dirName = [NSString stringWithFormat:@"%@/" , docId];
    [_datasource delete:dirName];
    ZipArchive *zip = [[ZipArchive new] autorelease];
    if ( [zip UnzipOpenFile:zipName] ) {
        [zip UnzipFileTo:[_datasource getFullPath:dirName] overWrite:YES];
        [zip UnzipCloseFile];
    }

	// baseUrlを保存する
	NSString *baseUrl = [request.userInfo objectForKey:@"baseUrl"];
	[_datasource saveBaseUrl:baseUrl documentId:docId];
	
	
    // Take care this way is depended on the fact 2x2 is max
    [self updateDownloadStatus:docId page:-1 px:1 py:1];

    [self downloadPage:docId page:0 px:0 py:0];
    
    if ( [delegate respondsToSelector:@selector(didMetaInfoDownloadFinished:)] ) {
        [delegate didMetaInfoDownloadFinished:docId];
    }
}

- (void)didPageDownloadFinished:(ASIHTTPRequest *)request {
    id docId = [request.userInfo objectForKey:@"id"];
    int page = [[request.userInfo objectForKey:@"page"] intValue];
    int px = [[request.userInfo objectForKey:@"px"] intValue];
    int py = [[request.userInfo objectForKey:@"py"] intValue];
    
    [self updateDownloadStatus:docId page:page px:px py:py];

    [self downloadNextPage:docId page:page px:px py:py];
}

- (void)didPageDownloadFailed:(ASIHTTPRequest *)request
{
    id docId = [request.userInfo objectForKey:@"id"];
	
	// TODO きちんとエラー情報を渡すこと
	if ( [delegate respondsToSelector:@selector(didPageDownloadFailed:error:)] ) {
		[delegate didPageDownloadFailed:docId error:[request error]];
	}
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}


#pragma mark ASIHTTPRequestDelegate

- (void)requestFailed:(ASIHTTPRequest *)request
{
	NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}

@end
