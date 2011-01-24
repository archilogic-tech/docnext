//
//  ImageSearchViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/23.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentContext.h"

@protocol ImageSearchDelegate

- (void)didImageSearchCanceled;
- (void)didImageSearchCompleted:(int)page ranges:(NSArray *)ranges selectedIndex:(int)selectedIndex;

@end


@interface ImageSearchViewController : UIViewController <UISearchBarDelegate , UITableViewDataSource , UITableViewDelegate> {
    UISearchBar *searchBar;
    UITableView *tableView;
    
    NSMutableArray *pages;
    NSMutableArray *ranges;

	id<DocumentViewerDatasource> _datasource;
	id<NSObject> _documentContext;
	id<ImageSearchDelegate> _delegate;

	//    ImageViewController *parent;
	//    id docId;
}

@property(nonatomic,retain) IBOutlet UISearchBar *searchBar;
@property(nonatomic,retain) IBOutlet UITableView *tableView;
//@property(nonatomic,assign) ImageViewController *parent;
//@property(nonatomic,assign) id docId;

@property (nonatomic, retain) id<DocumentViewerDatasource> datasource;
@property (nonatomic, retain) id<NSObject> documentContext;

@property (nonatomic, assign) id<ImageSearchDelegate> delegate;

@end
