//
//  BookshelfTableViewCell.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/21.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface BookshelfTableViewCell : UITableViewCell {
    UIImageView *imageView;
    UILabel *titleLabel;
    UILabel *publisherLabel;
    UILabel *pagesLabel;
}

@property(nonatomic,retain) IBOutlet UIImageView *imageView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UILabel *publisherLabel;
@property(nonatomic,retain) IBOutlet UILabel *pagesLabel;

- (void)apply:(int)docId;

@end
