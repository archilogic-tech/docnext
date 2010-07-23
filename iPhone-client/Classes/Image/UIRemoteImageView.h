//
//  UIRemoteImageView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ASIHTTPRequest.h"

@interface UIRemoteImageView : UIImageView {
    ASIHTTPRequest *request;
    id delegate;
}

@property(nonatomic,retain) ASIHTTPRequest *request;
@property(nonatomic,assign) id delegate;

- (void)load:(int)documentId page:(int)page level:(int)level px:(int)px py:(int)py;

@end
