//
//  ThumbnailViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ThumbnailViewController.h"

@interface ThumbnailViewController ()
- (void)setLabels:(int)index;
@end

@implementation ThumbnailViewController

@synthesize datasource = _datasource;
@synthesize documentContext = _documentContext;

#define currentView ((ThumbnailView*)self.view)



- (void)orientationChange:(UIInterfaceOrientation)toInterfaceOrientation
{
	if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
		self.view = _landscapeView;
	} else {
		self.view = _portraitView;
	}
	
	
	FlowCoverView *f = currentView.flowCoverView;
    f.offset = _documentContext.currentPage;
    [f draw];
    
	UISlider *s = currentView.pageSlider;
    s.minimumValue = 0.0;
    s.maximumValue = [_documentContext totalPage] - 1.0;
    s.value = _documentContext.currentPage;
	
    [self setLabels:_documentContext.currentPage];
}

- (void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
	[self orientationChange:toInterfaceOrientation];
}




+ (ThumbnailViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource
{

	UIInterfaceOrientation o = [UIDevice currentDevice].orientation;
	
	ThumbnailViewController *ret = [[[ThumbnailViewController alloc] initWithNibName:
                                     [Util buildNibName:@"Thumbnail" orientation:o] bundle:nil] autorelease];

	ret.datasource = datasource;
    return ret;
}

- (IBAction)pageSliderChanged:(id)sender {
    int cover = floor( currentView.pageSlider.value + 0.5 );

	FlowCoverView *f = currentView.flowCoverView;
    if ( cover != f.offset ) {
        f.offset = cover;
        [f draw];

		_documentContext.currentPage = cover;
        [self setLabels:cover];
    }
}

#pragma mark FlowCoverViewDelegate

- (int)flowCoverNumberImages:(FlowCoverView *)view {
	return [_documentContext totalPage];//[_datasource pages:documentId];
}

- (UIImage *)flowCover:(FlowCoverView *)view cover:(int)cover {
	return [_documentContext thumbnailWithIndex:cover];
}

- (void)flowCover:(FlowCoverView *)view didSelect:(int)cover {
	_documentContext.currentPage = cover;
	[self.navigationController popViewControllerAnimated:YES];
}

- (void)flowCover:(FlowCoverView *)view changeCurrent:(int)cover {
	_documentContext.currentPage = cover;
    [self setLabels:cover];
    
    [currentView.pageSlider setValue:cover animated:YES];
}

- (void)setLabels:(int)index {
	currentView.titleLabel.text = [_documentContext titleWithPage:index];
    currentView.pageLabel.text = [NSString stringWithFormat:@"%d / %d" , index + 1 , [_documentContext totalPage]];
}

- (void)timerTicked:(NSTimer *)_timer {
    int cover = floor( currentView.pageSlider.value + 0.5 );
    
	FlowCoverView *f = currentView.flowCoverView;
    if ( cover != f.offset ) {
        f.offset = cover;
        [f draw];
        
		_documentContext.currentPage = cover;
        [self setLabels:cover];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	
	[self orientationChange:self.interfaceOrientation];
}


- (void)dealloc {
	[_portraitView release];
	[_landscapeView release];
	
	[_datasource release];
	[_documentContext release];
    
    [super dealloc];
}

@end
