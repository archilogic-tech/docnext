//
//  RemoteImageOperation.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/03.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIRemoteImageView.h"

@interface RemoteImageOperation : NSOperation {
    UIRemoteImageView *delegate;
	NSString *_url;
}

- (id)initWithParam:(UIRemoteImageView *)_delegate url:(NSString*)url;

@end
