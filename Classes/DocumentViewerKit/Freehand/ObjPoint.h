//
//  ObjPoint.h
//  FreehandDrawing
//
//  Created by Yoskaku Toyama on 10/09/17.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ObjPoint : NSObject {
    float _x;
    float _y;
}

@property(nonatomic) float x;
@property(nonatomic) float y;

+ (ObjPoint *)pointFromCGPoint:(CGPoint)point;
+ (ObjPoint *)pointFromDictionary:(NSDictionary *)dictionary;

- (CGPoint)toCGPoint;
- (NSDictionary *)toDictionary;

@end
