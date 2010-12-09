//
//  BookshelfViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IUIViewController.h"
#import "DocumentViewerDatasource.h"

@interface BookshelfViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;

    NSMutableArray *downloadedIds;
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (BookshelfViewController *)createViewController:(UIInterfaceOrientation)orientation datasource:(id<DocumentViewerDatasource>)datasource;

- (IBAction)continueReadingClick:(id)sender;


@end
