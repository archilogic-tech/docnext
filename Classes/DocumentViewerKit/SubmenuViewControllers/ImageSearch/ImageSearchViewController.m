//
//  ImageSearchViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/23.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ImageSearchViewController.h"

#import "NSString+Data.h"
#import "NSString+Search.h"
#import "RangeObject.h"
#import "SearchResult.h"
#import "DocumentSearchResult.h"

@implementation ImageSearchViewController

@synthesize searchBar;
@synthesize tableView;

@synthesize datasource = _datasource;
@synthesize documentContext = _documentContext;
@synthesize delegate = _delegate;

#pragma mark -
#pragma mark UISearchBarDelegate

- (void)searchBarSearchButtonClicked:(UISearchBar *)_searchBar {
    [searchBar resignFirstResponder];
    
	[_searchResult release];
	_searchResult = [[_documentContext imageTextSearch:searchBar.text] retain];
    
    [tableView reloadData];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {
	[_delegate didImageSearchCanceled];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return [_searchResult count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    DocumentSearchResult *dsr = [_searchResult objectAtIndex:section];
	return [dsr.ranges count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {

    
    DocumentSearchResult *dsr = [_searchResult objectAtIndex:section];
	int page = dsr.page;

    NSString *title = [_documentContext titleWithPage:page];
	return [NSString stringWithFormat:@"%@ - %d page", title, (page + 1)];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)_tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {

    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [_tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
    DocumentSearchResult *dsr = [_searchResult objectAtIndex:indexPath.section];
	SearchResult *sr = [dsr.ranges objectAtIndex:indexPath.row];
    cell.textLabel.text = sr.highlight;
    
    return cell;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {

	DocumentSearchResult *dsr = [_searchResult objectAtIndex:indexPath.section];

	NSArray *a = [NSArray arrayWithArray:dsr.ranges];

	[_delegate didImageSearchCompleted:dsr.page ranges:a selectedIndex:indexPath.row];
}

- (void)viewDidLoad {
    searchBar.delegate = self;
    tableView.dataSource = self;
    tableView.delegate = self;
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
    [searchBar becomeFirstResponder];
}


- (void)dealloc {
    [searchBar release];
    [tableView release];
	[_searchResult release];
    
    [super dealloc];
}

@end
