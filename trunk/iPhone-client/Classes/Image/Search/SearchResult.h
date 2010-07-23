//
//  SearchResult.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/23.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RangeObject.h"

@interface SearchResult : NSObject {
    RangeObject *range;
    NSString *highlight;
}

@property(nonatomic,retain) RangeObject *range;
@property(nonatomic,retain) NSString *highlight;

@end
