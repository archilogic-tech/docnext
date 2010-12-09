//
//  RangeObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

// NSObject alternate for NSRange
@interface RangeObject : NSObject {
    int location;
    int length;
}

@property(nonatomic) int location;
@property(nonatomic) int length;

+ (RangeObject *)range:(NSRange)range;
- (NSRange)range;

@end
