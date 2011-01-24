//
//  BookshelfDeletionViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MediaPlayer/MediaPlayer.h>
#import "DocumentViewerDatasource.h"

@interface BookshelfDeletionViewController : UIViewController <UITableViewDataSource , UITableViewDelegate> {

	UITableView *tableView;
    
    NSMutableArray *downloadedIds;
    
    MPMoviePlayerController *_movie;

	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;

@property(nonatomic,retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (BookshelfDeletionViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource;

- (IBAction)backButtonClick:(id)sender;
- (IBAction)movieButtonClick:(id)sender;

@end
