//
//  BookmarkViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapDocViewController.h"

@interface BookmarkViewController : IUIViewController<UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;

    NSMutableArray *bookmarks;
    
    int currentDocumentId;
    int currentPage;
    NSString *currentTitle;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic,retain) NSMutableArray *bookmarks;
@property(nonatomic) int currentDocumentId;
@property(nonatomic) int currentPage;
@property(nonatomic,retain) NSString *currentTitle;

+ (BookmarkViewController *)createViewController:(int)documentId page:(int)page title:(NSString *)title;

- (IBAction)backButtonClick:(id)sender;
- (IBAction)addButtonClick:(id)sender;

@end
