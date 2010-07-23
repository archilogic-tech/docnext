//
//  VTextView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ITextView.h"

@interface VTextView : ITextView {
}

+ (float)measureWidth:(NSString *)text textSizes:(NSArray *)textSizes baseFontSize:(float)baseFontSize height:(float)height;

@end
