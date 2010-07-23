//
//  HistoryObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HistoryObject : NSObject {
    int documentId;
    int page;
}

@property(nonatomic) int documentId;
@property(nonatomic) int page;

+ (HistoryObject *)objectWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)toDictionary;

@end
