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

@implementation ImageSearchViewController

@synthesize searchBar;
@synthesize tableView;
@synthesize parent;
@synthesize docId;

@synthesize datasource = _datasource;

- (NSArray *)buildRangesElem:(NSArray *)hitRanges text:(NSString *)text {
    NSMutableArray *rangesElem = [NSMutableArray arrayWithCapacity:0];

    for ( RangeObject *range in hitRanges ) {
        SearchResult *result = [[SearchResult new] autorelease];
        result.range = range;
        
        int begin = MAX( range.location - 10 , 0 );
        int end = MIN( range.location + range.length + 10 , text.length );
        result.highlight = [text substringWithRange:NSMakeRange(begin, end - begin)];
        
        [rangesElem addObject:result];
    }
    
    return rangesElem;
}

- (void)doSearch {
    int _pages = [_datasource pages:docId];
    for ( int page = 0 ; page < _pages ; page++ ) {
        NSString *text = [_datasource imageText:docId page:page];
        
        NSArray *res = [text search:searchBar.text];
        if ( res.count > 0 ) {
            [pages addObject:[NSNumber numberWithInt:page]];
            
            [ranges addObject:[self buildRangesElem:res text:text]];
        }
    }
}

#pragma mark -
#pragma mark UISearchBarDelegate

- (void)searchBarSearchButtonClicked:(UISearchBar *)_searchBar {
    [searchBar resignFirstResponder];
    
    [pages release];
    pages = [[NSMutableArray arrayWithCapacity:0] retain];
    [ranges release];
    ranges = [[NSMutableArray arrayWithCapacity:0] retain];

    [self doSearch];
    
    [tableView reloadData];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {
    [parent cancelSearch];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return [pages count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [[ranges objectAtIndex:section] count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    int page = [[pages objectAtIndex:section] intValue];
    return [NSString stringWithFormat:@"%@ - %d page" , [_datasource toc:docId page:page].text , (page + 1)];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)_tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [_tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
    cell.textLabel.text = ((SearchResult *)[[ranges objectAtIndex:indexPath.section] objectAtIndex:indexPath.row]).highlight;
    
    return cell;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray *_ranges = [NSMutableArray arrayWithCapacity:0];
    for ( SearchResult *res in [ranges objectAtIndex:indexPath.section] ) {
        [_ranges addObject:res.range];
    }
    
    [parent selectSearchResult:[[pages objectAtIndex:indexPath.section] intValue] ranges:_ranges selectedIndex:indexPath.row];
}

- (void)viewDidLoad {
    searchBar.delegate = self;
    tableView.dataSource = self;
    tableView.delegate = self;
    
    [searchBar becomeFirstResponder];
}

- (void)dealloc {
    [searchBar release];
    [tableView release];
    [pages release];
    [ranges release];
    
    [super dealloc];
}

@end
