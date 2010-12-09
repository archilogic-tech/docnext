//
//  ObjBezier.h
//  FreehandDrawing
//
//  Created by Yoskaku Toyama on 10/09/17.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ObjBezier : NSObject {
    CGPoint _p0;
    CGPoint _cp0;
    CGPoint _cp1;
    CGPoint _p1;
}

@property(nonatomic) CGPoint p0;
@property(nonatomic) CGPoint cp0;
@property(nonatomic) CGPoint cp1;
@property(nonatomic) CGPoint p1;

@end
