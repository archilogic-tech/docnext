//
//  ITextView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TextViewConfig.h"

@interface ITextView : UIView {
    NSString *text;
    NSArray *rubys;
    NSArray *textSizes;
    
    TextViewConfig *config;
}

@property(nonatomic,retain) NSString *text;
@property(nonatomic,retain) NSArray *rubys;
@property(nonatomic,retain) NSArray *textSizes;
@property(nonatomic,retain) TextViewConfig *config;

@end
