//
//  BookshelfTableViewCell.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/21.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookshelfTableViewCell.h"

@implementation BookshelfTableViewCell

@synthesize imageView;
@synthesize titleLabel;
@synthesize publisherLabel;
@synthesize pagesLabel;

@synthesize datasource = _datasource;

- (void)apply:(id)docId {
	
	self.imageView.image = [_datasource thumbnail:docId cover:0];
    self.titleLabel.text = [_datasource title:docId];
    self.publisherLabel.text = [_datasource publisher:docId];
    self.pagesLabel.text = [NSString stringWithFormat:@"%d page" , [_datasource pages:docId]];
}

- (void)dealloc {
    [imageView release];
    [titleLabel release];
    [publisherLabel release];
    
    [super dealloc];
}

@end
