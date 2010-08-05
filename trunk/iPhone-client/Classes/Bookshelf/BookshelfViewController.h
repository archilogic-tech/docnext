//
//  BookshelfViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IUIViewController.h"

@interface BookshelfViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;

    NSMutableArray *downloadedIds;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;

+ (BookshelfViewController *)createViewController:(UIInterfaceOrientation)orientation;

- (IBAction)continueReadingClick:(id)sender;

@end
