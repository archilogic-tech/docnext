//
//  MarkupParseResult.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/05.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface MarkupParseResult : NSObject {
    NSString *text;
    NSArray *rubys;
    NSArray *textSizes;
}

@property(nonatomic,retain) NSString *text;
@property(nonatomic,retain) NSArray *rubys;
@property(nonatomic,retain) NSArray *textSizes;

@end
