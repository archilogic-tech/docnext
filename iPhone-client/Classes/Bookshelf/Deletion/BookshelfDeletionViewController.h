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

@interface BookshelfDeletionViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;
    
    NSMutableArray *downloadedIds;
    
    MPMoviePlayerController *_movie;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;

+ (BookshelfDeletionViewController *)createViewController:(UIInterfaceOrientation)orientation;

- (IBAction)backButtonClick:(id)sender;
- (IBAction)movieButtonClick:(id)sender;

@end
