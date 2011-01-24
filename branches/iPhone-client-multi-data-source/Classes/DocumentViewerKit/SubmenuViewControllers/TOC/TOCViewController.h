//
//  TOCViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DocumentContext.h"

@interface TOCViewController : UIViewController <UITableViewDataSource , UITableViewDelegate> {

	UITableView *tableView;
    
    NSArray *tocs;

	DocumentContext *_documentContext;
	id<NSObject,DocumentViewerDatasource> _datasource;					
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
@property(nonatomic,retain) NSArray *tocs;

@property (nonatomic, retain) DocumentContext *documentContext;
@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (TOCViewController *)createViewController:(id<DocumentViewerDatasource>)datasource;

- (IBAction)backButtonClick:(id)sender;

@end
