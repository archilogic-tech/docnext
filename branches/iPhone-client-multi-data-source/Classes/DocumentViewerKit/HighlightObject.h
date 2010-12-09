//
//  HighlightObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/09/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HighlightObject : NSObject {
    int location;
    int length;
    int color;
    NSString *text;
}

@property(nonatomic) int location;
@property(nonatomic) int length;
@property(nonatomic) int color;
@property(nonatomic,retain) NSString *text;

+ (HighlightObject *)objectWithDictionary:(NSDictionary *)dictionary;

- (NSDictionary *)toDictionary;
- (NSRange)range;

@end
