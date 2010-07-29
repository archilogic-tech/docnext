//
//  RegionInfo.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Region.h"

@interface RegionInfo : NSObject {
    Region *region;
    int index;
}

@property(nonatomic,retain) Region *region;
@property(nonatomic) int index;

@end
