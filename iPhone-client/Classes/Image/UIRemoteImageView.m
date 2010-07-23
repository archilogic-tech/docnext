//
//  UIRemoteImageView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "UIRemoteImageView.h"
#import "ASIHTTPRequest.h"
#import "Const.h"

@implementation UIRemoteImageView

@synthesize request;
@synthesize delegate;

- (void)load:(int)documentId page:(int)page level:(int)level px:(int)px py:(int)py {
    [self.request cancel];
 
    NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%d&page=%d&level=%d&px=%d&py=%d" ,
                     ServerEndpoint , UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone" ,
                     documentId , page , level , px , py];
    self.request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    self.request.delegate = self;
    self.request.didFinishSelector = @selector(getImageFinish:);
    
    [self.request startAsynchronous];
}

- (void)getImageFinish:(ASIHTTPRequest *)_request {
    self.image = [UIImage imageWithData:[_request responseData]];
    self.alpha = 0.0;
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.5];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self cache:NO];
    [UIView setAnimationDelegate:self];
    [UIView setAnimationDidStopSelector:@selector(animationDidFinish:finished:context:)];
    
    self.alpha = 1.0;
    
    [UIView commitAnimations];
}

- (void)animationDidFinish:(NSString *)animationID finished:(NSNumber *)finished context:(void *)context {
    [delegate performSelector:@selector(remoteImageViewDidFinish)];
}

- (void)requestFailed:(ASIHTTPRequest *)_request {
    NSLog( @"Http request error %@" , [[_request error] localizedDescription] );
}

- (void)dealloc {
    [request cancel];
    
    [request release];
    
    [super dealloc];
}

@end
