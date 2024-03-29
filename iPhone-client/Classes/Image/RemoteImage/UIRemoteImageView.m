//
//  UIRemoteImageView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "UIRemoteImageView.h"
#import "RemoteImageOperation.h"

@implementation UIRemoteImageView

- (void)load:(NSOperationQueue *)queue docId:(int)docId page:(int)page level:(int)level px:(int)px py:(int)py {
    [queue addOperation:
     [[[RemoteImageOperation alloc] initWithParam:self docId:docId page:page level:level px:px py:py] autorelease]];
}

- (void)didFinishFetch:(UIImage *)image {
    self.image = image;
    self.alpha = 0.0;
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.5];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self cache:NO];
    
    self.alpha = 1.0;
    
    [UIView commitAnimations];
}

@end
