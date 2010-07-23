//
//  BookshelfTableViewCell.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/21.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookshelfTableViewCell.h"
#import "FileUtil.h"

@implementation BookshelfTableViewCell

@synthesize imageView;
@synthesize titleLabel;
@synthesize publisherLabel;
@synthesize pagesLabel;

- (void)apply:(int)docId {
    self.imageView.image = [UIImage imageWithContentsOfFile:[FileUtil getFullPath:[NSString stringWithFormat:@"%d/images/thumb-%d.jpg" , docId , 0]]];
    self.titleLabel.text = [FileUtil title:docId];
    self.publisherLabel.text = [FileUtil publisher:docId];
    self.pagesLabel.text = [NSString stringWithFormat:@"%d page" , [FileUtil pages:docId]];
}

- (void)dealloc {
    [imageView release];
    [titleLabel release];
    [publisherLabel release];
    
    [super dealloc];
}

@end
