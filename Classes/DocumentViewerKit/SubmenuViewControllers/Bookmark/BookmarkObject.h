//
//  BookmarkObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DocumentContext.h"


@interface BookmarkObject : NSObject {
//    id documentId;
//    int page;
    
    // fields for display
    NSString *contentName;

	DocumentContext *_documentContext;
}

//@property(nonatomic, copy) id documentId;
//@property(nonatomic) int page;
@property (nonatomic, retain) NSString *contentName;
@property (nonatomic, retain) DocumentContext *documentContext;

+ (BookmarkObject *)objectWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)toDictionary;
- (BOOL)equals:(BookmarkObject *)obj;

@end
