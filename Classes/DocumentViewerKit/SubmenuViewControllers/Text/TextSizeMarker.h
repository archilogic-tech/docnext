//
//  TextSizeMarker.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TextSizeMarker : NSObject {
    float factor;
    int line;
}

@property(nonatomic) float factor;
@property(nonatomic) int line;

+ (TextSizeMarker *)markerWithData:(float)factor line:(int)line;
+ (TextSizeMarker *)markerWithDictionary:(NSDictionary *)dictionary;

@end
