//
//  DownloadStatusObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DownloadStatusObject : NSObject {
	id<NSObject> metaDocumentId;
    id<NSObject> docId;
    int downloadedPage;
    int downloadedPx;
    int downloadedPy;
	int currentDocumentOffset;
}

@property (nonatomic, assign) id<NSObject> metaDocumentId;
@property(nonatomic, assign) id<NSObject> docId;
@property(nonatomic) int downloadedPage;
@property(nonatomic) int downloadedPx;
@property(nonatomic) int downloadedPy;
@property(nonatomic) int currentDocumentOffset;

+ (DownloadStatusObject *)objectWithDictionary:(NSDictionary *)dictionary;

- (NSDictionary *)toDictionary;

@end