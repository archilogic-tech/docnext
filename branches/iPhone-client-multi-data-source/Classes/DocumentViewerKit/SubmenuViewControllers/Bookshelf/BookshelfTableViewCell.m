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

- (void)apply:(id<NSObject>)docId {
	
	DocumentContext *dc = [[DocumentContext alloc] init];
	dc.documentId = docId;
	
	self.imageView.image = [dc thumbnailWithIndex:0];//  [_datasource thumbnail:docId cover:0];
    self.titleLabel.text = [dc titleWithPage:0]; //[_datasource title:docId];
    self.publisherLabel.text = [dc publisher];
    self.pagesLabel.text = [NSString stringWithFormat:@"%d page" , [dc totalPage]];

	[dc release];
}

- (void)dealloc {
    [imageView release];
    [titleLabel release];
    [publisherLabel release];
    
    [super dealloc];
}

@end
