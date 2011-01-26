//
//  BookmarkViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentViewerDatasource.h"
#import "DocumentContext.h"

@interface BookmarkViewController : UIViewController<UITableViewDataSource , UITableViewDelegate> {

	UITableView *tableView;

    NSMutableArray *bookmarks;
    
	id<NSObject,DocumentViewerDatasource> _datasource;
	DocumentContext *_documentContext;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic,retain) NSMutableArray *bookmarks;

@property (nonatomic, copy) DocumentContext *documentContext;
@property (nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (BookmarkViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource;

- (IBAction)backButtonClick:(id)sender;
- (IBAction)addButtonClick:(id)sender;

@end
