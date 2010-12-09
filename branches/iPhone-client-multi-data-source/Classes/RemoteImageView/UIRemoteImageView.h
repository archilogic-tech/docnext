//
//  UIRemoteImageView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIRemoteImageView : UIImageView {
}

- (void)load:(NSOperationQueue *)queue url:(NSString*)url;
- (void)didFinishFetch:(UIImage *)image;

@end
