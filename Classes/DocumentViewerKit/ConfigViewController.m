//
//  ConfigViewController.m
//  MapDoc
//
//  Created by sakukawa on 11/01/26.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "ConfigViewController.h"

#import "ImageViewController.h"
#import "TOCViewController.h"
#import "ThumbnailViewController.h"
#import "BookmarkViewController.h"
#import "TextViewController.h"
#import "ObjPoint.h"


@implementation ConfigViewController

@synthesize parent = _parent;

// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];

	[_parent.view addSubview:self.view];
}

- (void) viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	titleLabel.text = [_parent.documentContext documentTitle];
}


/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[_freehandSwitch release];

    [super dealloc];
}


- (IBAction)homeButtonClick:(id)sender {
    [_parent.navigationController popToRootViewControllerAnimated:YES];
}

- (IBAction)tocViewButtonClick:(id)sender
{
	TOCViewController *tc = [TOCViewController createViewController:_parent.datasource];
	tc.documentContext = _parent.documentContext;
	[_parent.navigationController pushViewController:tc animated:YES];
}

- (IBAction)thumbnailViewButtonClick:(id)sender
{
	ThumbnailViewController *c = [ThumbnailViewController createViewController:_parent.datasource];
	c.documentContext = _parent.documentContext;
	[_parent.navigationController pushViewController:c animated:YES];
}

- (IBAction)bookmarkViewButtonClick:(id)sender
{
	BookmarkViewController *c = [BookmarkViewController createViewController:_parent.datasource];
	c.documentContext = _parent.documentContext;
	[_parent.navigationController pushViewController:c animated:YES];
}

- (IBAction)textViewButtonClick:(id)sender
{
	TextViewController *c = [TextViewController createViewController];
	c.documentContext = _parent.documentContext;
	[_parent.navigationController pushViewController:c animated:YES];
}

- (IBAction)tweetButtonClick:(id)sender {
    NSURL *url = [NSURL URLWithString:[[NSString stringWithFormat:@"http://twitter.com/home?status=Sample tweet"]
                                       stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    [[UIApplication sharedApplication] openURL:url];
}

- (IBAction)searchButtonClick:(id)sender
{
    BOOL isLand = UIInterfaceOrientationIsLandscape( self.interfaceOrientation );
    
    if ( isLand ) {
        [[[[UIAlertView alloc] initWithTitle:@"Search function is disabled currently on landscape orientation"
                                     message:nil delegate:nil cancelButtonTitle:@"OK"
                           otherButtonTitles:nil] autorelease] show];
        return;
    }
    
    NSString *orientation = isLand ? @"-land" : @"";
    ImageSearchViewController *searchViewController = [[ImageSearchViewController alloc]
													   initWithNibName:[NSString stringWithFormat:@"ImageSearchViewController%@" , orientation] bundle:nil];
	searchViewController.delegate = self;
	searchViewController.documentContext = _parent.documentContext;
    
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        popover = [[UIPopoverController alloc] initWithContentViewController:searchViewController];
        popover.popoverContentSize = isLand ? CGSizeMake(480, 320) : CGSizeMake(320, 480);
        [popover presentPopoverFromRect:((UIView *)sender).frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    } else {
		// TODO searchだからmodalの方がよいか?
		[_parent.navigationController pushViewController:searchViewController animated:YES];
        //[self.view addSubview:searchViewController.view];
    }
	[searchViewController release];
}

- (void)saveFreehand {
	
	NSMutableArray *buf = [NSMutableArray arrayWithCapacity:0];
	for ( NSArray *stroke in _parent.freehandView.points ) {
		NSMutableArray *strokeBuf = [NSMutableArray arrayWithCapacity:0];
		for ( ObjPoint *point in stroke ) {
			[strokeBuf addObject:[point toDictionary]];
		}
		[buf addObject:strokeBuf];
	}
	
	[_parent.datasource saveFreehand:_parent.documentContext.documentId
								page:_parent.documentContext.currentPage
								data:buf];
}


- (IBAction)freehandUndoClick {
    [_parent.freehandView undo];
    [self saveFreehand];
}

- (IBAction)freehandClearClick {
    [_parent.freehandView clear];
    [self saveFreehand];
}

- (IBAction)freehandSwitchChanged {
//    _parent.tiledScrollView.scrollEnabled = !_freehandSwitch.on;
    _parent.tiledScrollViewContainer.userInteractionEnabled = !_freehandSwitch.on;
    _parent.freehandView.userInteractionEnabled = _freehandSwitch.on;
    
    [self saveFreehand];
}


#pragma mark ImageSearchDelegate

- (void)didImageSearchCompleted:(int)page ranges:(NSArray *)ranges selectedIndex:(int)selectedIndex
{
    [self didImageSearchCanceled];
	
	BOOL isLeft = (page > _parent.documentContext.currentPage);
	_parent.documentContext.currentPage = page;
	[_parent movePageToCurrent:isLeft ? PageTransitionFromLeft : PageTransitionFromRight];
	
    [_parent.overlayManager showSearchResult:ranges selectedIndex:selectedIndex];
	
}

- (void)didImageSearchCanceled
{
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        [popover dismissPopoverAnimated:YES];
        [popover release];
        popover = nil;
    } else {
		[_parent.navigationController popViewControllerAnimated:YES];
        //[searchViewController.view removeFromSuperview];
    }
	[_parent setHideConfigView:YES];
}


@end
