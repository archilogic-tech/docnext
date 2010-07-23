//
//  Region.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface Region : NSObject {
    double x;
    double y;
    double width;
    double height;
}

@property(nonatomic) double x;
@property(nonatomic) double y;
@property(nonatomic) double width;
@property(nonatomic) double height;

+ (Region *)objectWithDictionary:(NSDictionary *)dictionary;

@end
