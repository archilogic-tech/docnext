//
//  PageTextInfo.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface PageTextInfo : NSObject {
    NSString *text;
    NSArray *regions;
}

@property(nonatomic,retain) NSString *text;
@property(nonatomic,retain) NSArray *regions;

+ (PageTextInfo *)objectWithDictionary:(NSDictionary *)dictionary;

@end
