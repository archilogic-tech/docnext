//
//  HTextView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ITextView.h"

@interface HTextView : ITextView {
}

+ (float)measureHeight:(NSString *)text textSizes:(NSArray *)textSizes baseFontSize:(float)baseFontSize width:(float)width;

@end
