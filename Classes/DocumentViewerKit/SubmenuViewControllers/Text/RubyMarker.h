//
//  RubyObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RubyMarker : NSObject {
    NSString *text;
    NSRange range;
}

@property(nonatomic,retain) NSString *text;
@property(nonatomic) NSRange range;

+ (RubyMarker *) markerWithData:(NSString *)text location:(int)location length:(int)length;
+ (RubyMarker *) markerWithDictionary:(NSDictionary *)dictionary;

@end
