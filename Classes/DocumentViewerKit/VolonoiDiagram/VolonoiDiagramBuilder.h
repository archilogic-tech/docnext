//
//  VolonoiDiagramBuilder.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface VolonoiDiagramBuilder : NSObject {
}

+ (NSArray *)build:(NSArray *)regions width:(int)width height:(int)height actual:(CGRect)actual;

@end
