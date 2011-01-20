//
//  BookmarkViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapDocViewController.h"
#import "DocumentViewerDatasource.h"
#import "DocumentContext.h"

@interface BookmarkViewController : IUIViewController<UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;

    NSMutableArray *bookmarks;
    
//    id<NSObject> currentDocumentId;
//    int currentPage;
//    NSString *currentTitle;

	id<NSObject,DocumentViewerDatasource> _datasource;
	DocumentContext *_documentContext;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic,retain) NSMutableArray *bookmarks;

//@property(nonatomic,copy) id<NSObject> currentDocumentId;
//@property(nonatomic) int currentPage;
//@property(nonatomic,retain) NSString *currentTitle;

@property (nonatomic, retain) DocumentContext *documentContext;
@property (nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;


+ (BookmarkViewController *)createViewController:(UIInterfaceOrientation)orientation
									  datasource:(id<NSObject,DocumentViewerDatasource>)datasource;

- (IBAction)backButtonClick:(id)sender;
- (IBAction)addButtonClick:(id)sender;

@end
