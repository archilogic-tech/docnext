//
//  Util.m
//  MapDoc
//
//  Created by sakukawa on 11/01/24.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "Util.h"


@implementation Util

+ (NSString *)buildNibName:(NSString *)prefix orientation:(UIInterfaceOrientation)orientation {
    NSString *target = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"-iPad" : @"-iPhone";
    NSString *orient = @"";//UIInterfaceOrientationIsLandscape( orientation ) ? @"-land" : @"";
    return [NSString stringWithFormat:@"%@ViewController%@%@" , prefix , target , orient];
}

@end
