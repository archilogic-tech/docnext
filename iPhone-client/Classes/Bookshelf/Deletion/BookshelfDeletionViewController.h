//
//  BookshelfDeletionViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IUIViewController.h"

@interface BookshelfDeletionViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;
    
    NSMutableArray *downloadedIds;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;

+ (BookshelfDeletionViewController *)createViewController;

- (IBAction)backButtonClick:(id)sender;

@end
