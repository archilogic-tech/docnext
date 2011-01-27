    //
//  BookshelfDeletionViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "BookshelfDeletionViewController.h"
#import "BookshelfTableViewCell.h"

@implementation BookshelfDeletionViewController

@synthesize tableView;

@synthesize datasource = _datasource;

+ (BookshelfDeletionViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource
{
	UIInterfaceOrientation o = [UIDevice currentDevice].orientation;
	
    BookshelfDeletionViewController *ret = [[[BookshelfDeletionViewController alloc] initWithNibName:
                                             [Util buildNibName:@"BookshelfDeletion" orientation:o] bundle:nil] autorelease];
	ret.datasource = datasource;
    return ret;
}


- (IBAction)backButtonClick:(id)sender {
	[self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)movieButtonClick:(id)sender {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(movieDuratoinAvailable:) name:MPMovieDurationAvailableNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(movieMediaTypesAvailable:) name:MPMovieMediaTypesAvailableNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(movieNaturalSizeAvailable:) name:MPMovieNaturalSizeAvailableNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerDidEnterFullscreen:) name:MPMoviePlayerDidEnterFullscreenNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerDidExitFullscreen:) name:MPMoviePlayerDidExitFullscreenNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerLoadStateDidChange:) name:MPMoviePlayerLoadStateDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerNowPlayingMovieDidChange:) name:MPMoviePlayerNowPlayingMovieDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerPlaybackDidFinish:) name:MPMoviePlayerPlaybackDidFinishNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerPlaybackStateDidChange:) name:MPMoviePlayerPlaybackStateDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerThumbnailImageRequestDidFinish:) name:MPMoviePlayerThumbnailImageRequestDidFinishNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerWillEnterFullscreen:) name:MPMoviePlayerWillEnterFullscreenNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayerWillExitFullscreen:) name:MPMoviePlayerWillExitFullscreenNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(movieSourceTypeAvailable:) name:MPMovieSourceTypeAvailableNotification object:nil];
    
    _movie = [[MPMoviePlayerController alloc] initWithContentURL:
              [NSURL URLWithString:@"http://ustdoc.com/docman_optimage/video/prog_index.m3u8"]];
    
    _movie.controlStyle = MPMovieControlStyleFullscreen;
    _movie.view.frame = self.view.frame;
    [self.view addSubview:_movie.view];
    
    [_movie play];
    /*
    MPMoviePlayerViewController *mov = [[MPMoviePlayerViewController alloc] initWithContentURL:
                                        [NSURL URLWithString:@"http://ustdoc.com/docman_optimage/video/prog_index.m3u8"]];
    [self presentMoviePlayerViewControllerAnimated:mov];
    [mov.moviePlayer play];
     */
}

#pragma mark MPMovieNotificatoin

- (void)movieDuratoinAvailable:(NSNotification *)notification {
    NSLog(@"MovieDurationAvaiable");
}

- (void)movieMediaTypesAvailable:(NSNotification *)notification {
    NSLog(@"MovieMediaTypesAvaiable");
}

- (void)movieNaturalSizeAvailable:(NSNotification *)notification {
    NSLog(@"MovieNaturalSizeAvaiable");
}

- (void)moviePlayerDidEnterFullscreen:(NSNotification *)notification {
    NSLog(@"MoviePlayerDidEnterFullscreen");
}

- (void)moviePlayerDidExitFullscreen:(NSNotification *)notification {
    NSLog(@"MoviePlayerDidExitFullscreen");
}

- (void)moviePlayerLoadStateDidChange:(NSNotification *)notification {
    NSLog(@"MoviePlayerLoadStateDidChange");
}

- (void)moviePlayerNowPlayingMovieDidChange:(NSNotification *)notification {
    NSLog(@"MoviePlayerNowPlayingMovieDidChange");
}

- (void)moviePlayerPlaybackDidFinish:(NSNotification *)notification {
    NSLog(@"MoviePlayerPlaybackDidFinish");
    
    [_movie.view removeFromSuperview];
    [_movie stop];
    [_movie release];
    _movie = nil;
}

- (void)moviePlayerPlaybackStateDidChange:(NSNotification *)notification {
    MPMoviePlayerController *movie = notification.object;
    NSLog(@"MoviePlayerPlaybackStateDidChange: playbackState: %d , object: %@ , userInfo: %@" , movie.playbackState , notification.object , notification.userInfo);
}

- (void)moviePlayerScalingModeDidChange:(NSNotification *)notification {
    NSLog(@"MoviePlayerScalingModeDidChange");
}

- (void)moviePlayerThumbnailImageRequestDidFinish:(NSNotification *)notification {
    NSLog(@"MoviePlayerThumbnailImageRequesteDidFinish");
}

- (void)moviePlayerWillEnterFullscreen:(NSNotification *)notification {
    NSLog(@"MoviePlayerWillEnterFullscreen");
}

- (void)moviePlayerWillExitFullscreen:(NSNotification *)notification {
    NSLog(@"MoviePlayerWillExitFullscreen");
}

- (void)movieSourceTypeAvailable:(NSNotification *)notification {
    NSLog(@"MovieSourceTypeAvailable");
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
		cell.datasource = _datasource;
		cell.selectionStyle = UITableViewCellSelectionStyleNone;
    }
    
	id<NSObject> metaDocumentId = [downloadedIds objectAtIndex:indexPath.row];
    [cell apply:metaDocumentId];
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 128.0;
}

- (void)tableView:(UITableView *)_tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {

        id<NSObject> metaDocumentId = [downloadedIds objectAtIndex:indexPath.row];
		[_datasource deleteCache:metaDocumentId];

        [downloadedIds removeObjectAtIndex:indexPath.row];
        
        [_tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)_tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	// なにもしない?
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    
    self.tableView.editing = YES;
    self.tableView.allowsSelectionDuringEditing = YES;
    
	NSArray *tmp = [_datasource downloadedIds];
	downloadedIds = [[NSMutableArray alloc] init];
	for (NSString *s in tmp){
		[downloadedIds addObject:[s componentsSeparatedByString:@","]];
	}
    
    [self.tableView reloadData];
}

- (void)dealloc {
    [tableView release];
    [downloadedIds release];
	[_datasource release];
    
    [super dealloc];
}

@end
