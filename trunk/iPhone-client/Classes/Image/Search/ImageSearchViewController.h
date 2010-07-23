//
//  ImageSearchViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/23.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ImageViewController.h"

@interface ImageSearchViewController : UIViewController <UISearchBarDelegate , UITableViewDataSource , UITableViewDelegate> {
    UISearchBar *searchBar;
    UITableView *tableView;
    
    ImageViewController *parent;
    int docId;
    NSMutableArray *pages;
    NSMutableArray *ranges;
}

@property(nonatomic,retain) IBOutlet UISearchBar *searchBar;
@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic,assign) ImageViewController *parent;
@property(nonatomic) int docId;

@end
