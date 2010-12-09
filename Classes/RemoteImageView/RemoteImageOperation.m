//
//  RemoteImageOperation.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/03.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RemoteImageOperation.h"
#import "DocumentViewerConst.h"
#import "ASIHTTPRequest.h"

@implementation RemoteImageOperation

- (id)initWithParam:(UIRemoteImageView *)_delegate url:(NSString*)url
{
	if ((self = [super init])) {
        delegate = [_delegate retain];
		_url = [url retain];
    }
    return self;
}

- (void)dealloc {
    [delegate release];
	[_url release];
    [super dealloc];
}

- (void)main {

    NSLog(@"url : %@", _url);
	ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:_url]];
	request.requestHeaders = [NSMutableDictionary dictionaryWithObjectsAndKeys:@"docnext", @"User-Agent", nil];

    [request startSynchronous];

    [delegate performSelectorOnMainThread:@selector(didFinishFetch:)
                               withObject:[UIImage imageWithData:[request responseData]] waitUntilDone:YES];
}

@end
