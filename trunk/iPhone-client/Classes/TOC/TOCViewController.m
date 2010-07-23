    //
//  TOCViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TOCViewController.h"
#import "Const.h"
#import "ASIHTTPRequest.h"
#import "NSString+Data.h"
#import "TOCObject.h"
#import "JSON.h"
#import "FileUtil.h"

@implementation TOCViewController

@synthesize tableView;
@synthesize documentId;
@synthesize prevPage;
@synthesize tocs;

+ (TOCViewController *)createViewController:(int)documentId prevPage:(int)prevPage {
    TOCViewController *ret = [[[TOCViewController alloc] initWithNibName:[IUIViewController buildNibName:@"TOC"] bundle:nil] autorelease];
    [ret setLandspace];
    ret.documentId = documentId;
    ret.prevPage = prevPage;
    return ret;
}

- (IBAction)backButtonClick:(id)sender {
    [parent showImage:self.documentId page:self.prevPage];
}

- (IUIViewController *)createViewController {
    return [TOCViewController createViewController:self.documentId prevPage:self.prevPage];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.tocs count];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)_tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [_tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
    cell.textLabel.text = ((TOCObject *)[self.tocs objectAtIndex:indexPath.row]).text;
    
    return cell;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    int page = ((TOCObject *)[self.tocs objectAtIndex:indexPath.row]).page;
    [self.parent showImage:documentId page:page];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;

    self.tocs = [FileUtil tocs:documentId];
    [self.tableView reloadData];
}

- (void)dealloc {
    [tableView release];
    [tocs release];
    
    [super dealloc];
}

@end
