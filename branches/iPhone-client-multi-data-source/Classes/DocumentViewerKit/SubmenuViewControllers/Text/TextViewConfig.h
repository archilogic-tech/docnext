//
//  TextViewConfig.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface TextViewConfig : NSObject {
    float paddingTop;
    float paddingBottom;
    float paddingLeft;
    float paddingRight;
    float lineSpacing;
    BOOL rubyEnable;
    float fontSize;
    int textColor;
    int backgroundColor;
    
    NSString *fontName;
}

@property(nonatomic) float paddingTop;
@property(nonatomic) float paddingBottom;
@property(nonatomic) float paddingLeft;
@property(nonatomic) float paddingRight;
@property(nonatomic) float lineSpacing;
@property(nonatomic) BOOL rubyEnable;
@property(nonatomic) float fontSize;
@property(nonatomic) int textColor;
@property(nonatomic) int backgroundColor;
@property(nonatomic,retain) NSString *fontName;

+ (TextViewConfig *)configHDefault;
+ (TextViewConfig *)configVDefault;

@end
