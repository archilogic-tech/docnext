//
//  BookshelfDeletionViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MediaPlayer/MediaPlayer.h>
#import "IUIViewController.h"
#import "DocumentViewerDatasource.h"

@interface BookshelfDeletionViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;
    
    NSMutableArray *downloadedIds;
    
    MPMoviePlayerController *_movie;

	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;

@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (BookshelfDeletionViewController *)createViewController:(UIInterfaceOrientation)orientation datasource:(id<DocumentViewerDatasource>)datasource;

- (IBAction)backButtonClick:(id)sender;
- (IBAction)movieButtonClick:(id)sender;

@end
