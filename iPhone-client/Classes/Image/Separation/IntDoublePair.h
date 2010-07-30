//
//  IntegerDoublePair.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface IntDoublePair : NSObject {
    int intValue;
    double doubleValue;
}

@property(nonatomic) int intValue;
@property(nonatomic) double doubleValue;

+ (IntDoublePair *)pairWithIntValue:(int)intValue doubleValue:(double)doubleValue;

@end
