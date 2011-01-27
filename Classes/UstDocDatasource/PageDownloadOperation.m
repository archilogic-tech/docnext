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


- (void)main
{
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:_url]];
	request.downloadDestinationPath = _destination;

//    request.delegate = self;
//    request.didFinishSelector = @selector(didPageDownloadFinished:);
//	request.didFailSelector = @selector(didPageDownloadFailed:);
	
//    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
/*
    [request.userInfo setValue:metaDocumentId forKey:@"metaDocumentId"];
    [request.userInfo setValue:docId forKey:@"documentId"];
    [request.userInfo setValue:[NSNumber numberWithInt:page] forKey:@"page"];
    [request.userInfo setValue:[NSNumber numberWithInt:px] forKey:@"px"];
    [request.userInfo setValue:[NSNumber numberWithInt:py] forKey:@"py"];
 */
	
    [request startSynchronous];

	// TODO エラーチェック
}

@end
