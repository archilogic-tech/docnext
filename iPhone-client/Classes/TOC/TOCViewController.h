//
//  TOCViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapDocViewController.h"

@interface TOCViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;
    
    int documentId;
    int prevPage;
    NSArray *tocs;
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic) int documentId;
@property(nonatomic) int prevPage;
@property(nonatomic,retain) NSArray *tocs;

+ (TOCViewController *)createViewController:(int)documentId prevPage:(int)prevPage;

- (IBAction)backButtonClick:(id)sender;

@end
