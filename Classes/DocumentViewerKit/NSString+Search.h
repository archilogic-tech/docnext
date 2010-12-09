//
//  NSStringSearch.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (SearchAdditions)

- (NSArray *)search:(NSString *)term;

@end
