    //
//  BookmarkViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/01.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookmarkViewController.h"
#import "BookmarkObject.h"
#import "NSString+Data.h"

@interface BookmarkViewController()
- (void)saveBookmarks;
@end

@implementation BookmarkViewController

@synthesize tableView;
@synthesize bookmarks;
//@synthesize currentDocumentId;
//@synthesize currentPage;
//@synthesize currentTitle;

@synthesize documentContext = _documentContext;
@synthesize datasource = _datasource;

+ (BookmarkViewController *)createViewController:(UIInterfaceOrientation)orientation
									  datasource:(id<NSObject,DocumentViewerDatasource>)datasource
{
    BookmarkViewController *ret = [[[BookmarkViewController alloc] initWithNibName:
                                    [IUIViewController buildNibName:@"Bookmark" orientation:orientation] bundle:nil] autorelease];
    [ret setLandspace:orientation];
	ret.datasource = datasource;
    return ret;
}


- (IBAction)backButtonClick:(id)sender {
	[parent showImage:_documentContext];
//    [parent showImage:self.currentDocumentId page:self.currentPage];
}

- (IBAction)addButtonClick:(id)sender {
    BookmarkObject *bookmark = [[BookmarkObject new] autorelease];

	bookmark.documentContext = _documentContext;
	bookmark.contentName = [_documentContext title];
//    bookmark.documentId = self.currentDocumentId;
//    bookmark.page = self.currentPage;
//    bookmark.contentName = self.currentTitle;
    
    for ( BookmarkObject *already in self.bookmarks ) {
        if ( [bookmark equals:already] ) {
            [[[[UIAlertView alloc] initWithTitle:@"This page is already bookmarked" message:nil delegate:nil
                               cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
            
            return;
        }
    }
    
    [self.bookmarks addObject:bookmark];
    [self saveBookmarks];
    
    [self.tableView reloadData];
}

- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation {
	BookmarkViewController *c = [BookmarkViewController createViewController:orientation datasource:_datasource];
	c.documentContext = _documentContext;
//	c.currentDocumentId = self.currentDocumentId;
//	c.currentPage = self.currentPage;
	return c;
}

#pragma mark load

- (void)loadBookmarks {
    self.bookmarks = [NSMutableArray arrayWithCapacity:0];
    
	NSArray *bookmark = [_datasource bookmark];
	
    for ( NSDictionary *dic in bookmark) {
        [self.bookmarks addObject:[BookmarkObject objectWithDictionary:dic]];
    }
    
    [self.tableView reloadData];
}

- (void)saveBookmarks {
    NSMutableArray *buf = [NSMutableArray arrayWithCapacity:0];
    
    for ( BookmarkObject *bookmark in self.bookmarks ) {
        [buf addObject:[bookmark toDictionary]];
    }
    
	[_datasource saveBookmark:buf];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.bookmarks count];
}

- (UITableViewCell *)tableView:(UITableView *)_tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [_tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
    }
    
    BookmarkObject *bookmark = (BookmarkObject *)[self.bookmarks objectAtIndex:indexPath.row];

	NSString *title = [bookmark.documentContext title];
	cell.textLabel.text = [NSString stringWithFormat:@"%@ - %@ - %d page" , title , bookmark.contentName , (bookmark.documentContext.currentPage + 1)];
    
    return cell;
}

- (void)tableView:(UITableView *)_tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [self.bookmarks removeObjectAtIndex:indexPath.row];
        
        [self saveBookmarks];
        
        [_tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
    }   
}

- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
    [self.bookmarks exchangeObjectAtIndex:fromIndexPath.row withObjectAtIndex:toIndexPath.row];

    [self saveBookmarks];
}

- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    BookmarkObject *bookmark = (BookmarkObject *)[self.bookmarks objectAtIndex:indexPath.row];
	[parent showImage:bookmark.documentContext];
//    [parent showImage:bookmark.documentId page:bookmark.page];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.dataSource = self;
    self.tableView.delegate = self;

    self.tableView.editing = YES;
    self.tableView.allowsSelectionDuringEditing = YES;
    
    [self loadBookmarks];
}

- (void)dealloc {
    [tableView release];
    [bookmarks release];
//    [currentTitle release];
	[_datasource release];
	[_documentContext release];
//	[currentDocumentId release];
    
    [super dealloc];
}


@end
