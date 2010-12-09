//
//  TextViewConfig.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TextViewConfig.h"


@implementation TextViewConfig

@synthesize paddingTop;
@synthesize paddingBottom;
@synthesize paddingLeft;
@synthesize paddingRight;
@synthesize lineSpacing;
@synthesize rubyEnable;
@synthesize fontSize;
@synthesize textColor;
@synthesize backgroundColor;
@synthesize fontName;

+ (TextViewConfig *)configHDefault {
    TextViewConfig *ret = [[TextViewConfig new] autorelease];
    
    ret.paddingTop = ret.paddingBottom = 20;
    ret.paddingLeft = ret.paddingRight = 20;
    ret.lineSpacing = 2;
    ret.rubyEnable = YES;
    ret.textColor = 0x000000;
    ret.backgroundColor = 0xffffff;
    ret.fontSize = 16;
    ret.fontName = @"HiraKakuProN-W3";
    
    return ret;
}

+ (TextViewConfig *)configVDefault {
    TextViewConfig *ret = [[TextViewConfig new] autorelease];
    
    ret.paddingTop = ret.paddingBottom = 20;
    ret.paddingLeft = ret.paddingRight = 20;
    ret.lineSpacing = 2;
    ret.rubyEnable = YES;
    ret.textColor = 0x000000;
    ret.backgroundColor = 0xffffff;
    ret.fontSize = 16;
    ret.fontName = @"HiraKakuProN-W3";
    
    return ret;
}

- (void)dealloc {
    [fontName release];
    
    [super dealloc];
}

@end
