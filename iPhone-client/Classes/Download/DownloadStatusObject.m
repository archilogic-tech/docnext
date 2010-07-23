//
//  DownloadStatusObject.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "DownloadStatusObject.h"

@implementation DownloadStatusObject

@synthesize docId;
@synthesize downloadedPage;
@synthesize downloadedPx;
@synthesize downloadedPy;

+ (DownloadStatusObject *)objectWithDictionary:(NSDictionary *)dictionary {
    DownloadStatusObject *ret = [[DownloadStatusObject new] autorelease];
    
    ret.docId = [[dictionary objectForKey:@"docId"] intValue];
    ret.downloadedPage = [[dictionary objectForKey:@"downloadedPage"] intValue];
    ret.downloadedPx = [[dictionary objectForKey:@"downloadedPx"] intValue];
    ret.downloadedPy = [[dictionary objectForKey:@"downloadedPy"] intValue];
    
    return ret;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:[NSString stringWithFormat:@"%d" , docId] forKey:@"docId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , downloadedPage] forKey:@"downloadedPage"];
    [ret setObject:[NSString stringWithFormat:@"%d" , downloadedPx] forKey:@"downloadedPx"];
    [ret setObject:[NSString stringWithFormat:@"%d" , downloadedPy] forKey:@"downloadedPy"];
    
    return ret;
}

@end
