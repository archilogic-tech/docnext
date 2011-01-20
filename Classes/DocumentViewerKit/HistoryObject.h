//
//  HistoryObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DocumentContext.h"

@interface HistoryObject : NSObject {
//    id<NSObject> documentId;
//    int page;

	DocumentContext *_documentContext;
}

@property (nonatomic, retain) DocumentContext *documentContext;

//@property(nonatomic, assign) id<NSObject> documentId;
//@property(nonatomic) int page;

+ (HistoryObject *)objectWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)toDictionary;

@end
