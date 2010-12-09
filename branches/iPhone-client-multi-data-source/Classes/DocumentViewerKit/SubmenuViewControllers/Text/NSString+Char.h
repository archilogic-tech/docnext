//
//  NSString+Char.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface NSString (CharAdditions)

+ (NSString *)stringWithChar:(unichar)c;
- (NSString *)charAt:(int)index;

@end
