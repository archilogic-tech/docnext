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
    int docId;
    int page;
    int level;
    int px;
    int py;
}

- (id)initWithParam:(UIRemoteImageView *)delegate docId:(int)docId page:(int)page level:(int)level px:(int)px py:(int)py;

@end
