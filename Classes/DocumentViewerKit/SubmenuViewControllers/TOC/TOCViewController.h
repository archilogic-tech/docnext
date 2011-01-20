//
//  TOCViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapDocViewController.h"
#import "DocumentContext.h"

@interface TOCViewController : IUIViewController <UITableViewDataSource , UITableViewDelegate> {
    UITableView *tableView;
    
    //id<NSObject> documentId;
    //int prevPage;
    NSArray *tocs;

	DocumentContext *_documentContext;
	id<NSObject,DocumentViewerDatasource> _datasource;					
}

@property(nonatomic,retain) IBOutlet UITableView *tableView;
//@property(nonatomic,copy) id<NSObject> documentId;
//@property(nonatomic) int prevPage;
@property(nonatomic,retain) NSArray *tocs;

@property (nonatomic, retain) DocumentContext *documentContext;
@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;

+ (TOCViewController *)createViewController:(UIInterfaceOrientation)orientation datasource:(id<DocumentViewerDatasource>)datasource;

- (IBAction)backButtonClick:(id)sender;

@end
