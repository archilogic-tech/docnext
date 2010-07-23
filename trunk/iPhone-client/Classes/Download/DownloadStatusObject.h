//
//  DownloadStatusObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DownloadStatusObject : NSObject {
    int docId;
    int downloadedPage;
    int downloadedPx;
    int downloadedPy;
}

@property(nonatomic) int docId;
@property(nonatomic) int downloadedPage;
@property(nonatomic) int downloadedPx;
@property(nonatomic) int downloadedPy;

+ (DownloadStatusObject *)objectWithDictionary:(NSDictionary *)dictionary;

- (NSDictionary *)toDictionary;

@end
