//
//  RemoteImageOperation.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/03.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RemoteImageOperation.h"
#import "Const.h"
#import "ASIHTTPRequest.h"
#import "FileUtil.h"

@implementation RemoteImageOperation

- (id)initWithParam:(UIRemoteImageView *)_delegate docId:(int)_docId page:(int)_page level:(int)_level px:(int)_px py:(int)_py {
    if ((self = [super init])) {
        delegate = [_delegate retain];
        docId = _docId;
        page = _page;
        level = _level;
        px = _px;
        py = _py;
    }
    
    return self;
}

- (void)dealloc {
    [delegate release];
    
    [super dealloc];
}

- (void)main {
    NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%d&page=%d&level=%d&px=%d&py=%d" ,
                     ServerEndpoint , (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone") ,
                     docId , page , level , px , py];
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    
    [request startSynchronous];

    [delegate performSelectorOnMainThread:@selector(didFinishFetch:)
                               withObject:[UIImage imageWithData:[request responseData]] waitUntilDone:YES];
    
    NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    NSString *fileName = [NSString stringWithFormat:@"%d/images/%@-%d-%d-%d-%d.jpg", docId, type, page,
                          level, px, py];
    
    [FileUtil write:[request responseData] toFile:fileName];
}

@end
