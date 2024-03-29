//
//  TOCObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface TOCObject : NSObject {
    int page;
    NSString *text;
}

@property(nonatomic) int page;
@property(nonatomic,retain) NSString *text;

+ (TOCObject *)objectWithDictionary:(NSDictionary *)dictionary;

@end
