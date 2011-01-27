//
//  PageDownloadOperation.m
//  MapDoc
//
//  Created by sakukawa on 11/01/27.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "PageDownloadOperation.h"
#import "ASIHTTPRequest.h"

@implementation PageDownloadOperation

@synthesize url = _url;
@synthesize destination = _destination;
/*
@synthesize metaDocumentId = _metaDocumentId;
@synthesize documentId = _documentId;
@synthesize downloadManager = _downloadManager;
*/
- (id) init
{
	self = [super init];
	if (self) {
//		_finished = NO;
	}
	return self;
}

- (void) dealloc
{
	[_url release];
	[_destination release];
	[super dealloc];
}

/*
- (BOOL) isFinished
{
	return _finished;
}
*/

- (void)didPageDownloadFinished:(ASIHTTPRequest *)request {
/*
    id<NSObject> docId = [request.userInfo objectForKey:@"documentId"];
	id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
	int page = [[request.userInfo objectForKey:@"page"] intValue];
    int px = [[request.userInfo objectForKey:@"px"] intValue];
    int py = [[request.userInfo objectForKey:@"py"] intValue];
	
    [self updateDownloadStatus:metaDocumentId documentId:docId page:page px:px py:py];
    [self downloadNextPage:metaDocumentId documentId:docId page:page px:px py:py];
 */
}

- (void)didPageDownloadFailed:(ASIHTTPRequest *)request
{
	/*
	id<NSObject> metaDocumentId = [request.userInfo objectForKey:@"metaDocumentId"];
	[delegate didPageDownloadFailed:metaDocumentId error:request.error];
	*/
}


- (void)main
{
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:_url]];
    request.delegate = self;
    request.didFinishSelector = @selector(didPageDownloadFinished:);
	request.didFailSelector = @selector(didPageDownloadFailed:);
	
	// 直接正しいところに書き込む
	request.downloadDestinationPath = _destination;
//    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
/*
    [request.userInfo setValue:metaDocumentId forKey:@"metaDocumentId"];
    [request.userInfo setValue:docId forKey:@"documentId"];
    [request.userInfo setValue:[NSNumber numberWithInt:page] forKey:@"page"];
    [request.userInfo setValue:[NSNumber numberWithInt:px] forKey:@"px"];
    [request.userInfo setValue:[NSNumber numberWithInt:py] forKey:@"py"];
 */
	
    [request startSynchronous];
}

@end
