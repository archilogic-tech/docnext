//
//  SeparationHolder.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SeparationHolder : NSObject {
    NSMutableArray *regionHashMap;
    NSMutableArray *indexHashMap;
}

- (id)initWithRegions:(NSArray *)regions;
- (int)nearestIndex:(CGPoint)point;

@end
