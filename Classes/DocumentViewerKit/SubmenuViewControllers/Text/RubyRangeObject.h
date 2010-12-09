//
//  RubyRangeObject.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/05.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RubyRangeObject : NSObject {
    float base;
    float begin;
    float end;
}

@property(nonatomic) float base;
@property(nonatomic) float begin;
@property(nonatomic) float end;

+ (RubyRangeObject *)objectWithData:(float)base begin:(float)begin end:(float)end;

@end
