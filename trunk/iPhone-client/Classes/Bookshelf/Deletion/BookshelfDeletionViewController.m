    //
//  BookshelfDeletionViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookshelfDeletionViewController.h"
#import "BookshelfTableViewCell.h"
#import "FileUtil.h"
#import "MapDocViewController.h"

@implementation BookshelfDeletionViewController

@synthesize tableView;

+ (BookshelfDeletionViewController *)createViewController {
    BookshelfDeletionViewController *ret = [[[BookshelfDeletionViewController alloc] initWithNibName:[IUIViewController buildNibName:@"BookshelfDeletion"] bundle:nil] autorelease];
    [ret setLandspace];
    return ret;
}

- (IUIViewController *)createViewController {
    return [BookshelfDeletionViewController createViewController];
}

- (IBAction)backButtonClick:(id)sender {
    [parent showHome:YES];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [downloadedIds count];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)_tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"BookshelfCell";
    
    BookshelfTableViewCell *cell = (BookshelfTableViewCell *)[_tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = (BookshelfTableViewCell *)[[[[UIViewController alloc] initWithNibName:@"BookshelfTableViewCell" bundle:nil] autorelease] view];
    }
    
    [cell apply:[[downloadedIds objectAtIndex:indexPath.row] intValue]];
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 128.0;
}

- (void)tableView:(UITableView *)_tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [FileUtil deleteCache:[[downloadedIds objectAtIndex:indexPath.row] intValue]];
        
        [downloadedIds removeObjectAtIndex:indexPath.row];
        
        [_tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
    }   
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)_tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    
    self.tableView.editing = YES;
    self.tableView.allowsSelectionDuringEditing = YES;
    
    downloadedIds = [[FileUtil downloadedIds] retain];
    
    [self.tableView reloadData];
}

- (void)dealloc {
    [tableView release];
    [downloadedIds release];
    
    [super dealloc];
}

@end
