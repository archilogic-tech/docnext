//
//  DownloadStatusObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "DownloadStatusObject.h"

@implementation DownloadStatusObject

@synthesize metaDocumentId;
@synthesize docId;
@synthesize downloadedPage;
@synthesize downloadedPx;
@synthesize downloadedPy;
@synthesize currentDocumentOffset;

+ (DownloadStatusObject *)objectWithDictionary:(NSDictionary *)dictionary {
    DownloadStatusObject *ret = [[DownloadStatusObject new] autorelease];
    
    ret.docId = [dictionary objectForKey:@"docId"];
	ret.metaDocumentId = [dictionary objectForKey:@"metaDocumentId"];
    ret.downloadedPage = [[dictionary objectForKey:@"downloadedPage"] intValue];
    ret.downloadedPx = [[dictionary objectForKey:@"downloadedPx"] intValue];
    ret.downloadedPy = [[dictionary objectForKey:@"downloadedPy"] intValue];
    ret.currentDocumentOffset = [[dictionary objectForKey:@"currentDocumentOffset"] intValue];
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:docId forKey:@"docId"];
	[ret setObject:metaDocumentId forKey:@"metaDocumentId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , downloadedPage] forKey:@"downloadedPage"];
    [ret setObject:[NSString stringWithFormat:@"%d" , downloadedPx] forKey:@"downloadedPx"];
    [ret setObject:[NSString stringWithFormat:@"%d" , downloadedPy] forKey:@"downloadedPy"];
    [ret setObject:[NSString stringWithFormat:@"%d" , currentDocumentOffset] forKey:@"currentDocumentOffset"];
    
    return ret;
}

@end
