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

- (void)load:(NSOperationQueue *)queue url:(NSString*)url
{
	RemoteImageOperation *op = [[RemoteImageOperation alloc] initWithParam:self url:url];
	[queue addOperation:op];
	[op release];
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
