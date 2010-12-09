//
//  BookshelfTableViewCell.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/21.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentViewerDatasource.h"

@interface BookshelfTableViewCell : UITableViewCell {
    UIImageView *imageView;
    UILabel *titleLabel;
    UILabel *publisherLabel;
    UILabel *pagesLabel;

	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet UIImageView *imageView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UILabel *publisherLabel;
@property(nonatomic,retain) IBOutlet UILabel *pagesLabel;

@property(nonatomic,assign) id<NSObject,DocumentViewerDatasource> datasource;

- (void)apply:(id)docId;

@end
