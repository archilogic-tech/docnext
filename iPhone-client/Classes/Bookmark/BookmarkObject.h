//
//  BookmarkObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface BookmarkObject : NSObject {
    int documentId;
    int page;
    
    // fields for display
    NSString *contentName;
}

@property(nonatomic) int documentId;
@property(nonatomic) int page;
@property(nonatomic,retain) NSString *contentName;

+ (BookmarkObject *)objectWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)toDictionary;
- (BOOL)equals:(BookmarkObject *)obj;

@end
