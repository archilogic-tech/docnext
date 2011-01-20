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

@synthesize flowCoverView;
@synthesize titleLabel;
@synthesize pageLabel;
@synthesize pageSlider;

@synthesize datasource = _datasource;
@synthesize documentContext = _documentContext;

+ (ThumbnailViewController *)createViewController:(UIInterfaceOrientation)orientation
									   datasource:(id<NSObject,DocumentViewerDatasource>)datasource
{

	ThumbnailViewController *ret = [[[ThumbnailViewController alloc] initWithNibName:
                                     [IUIViewController buildNibName:@"Thumbnail" orientation:orientation] bundle:nil] autorelease];

    [ret setLandspace:orientation];
	ret.datasource = datasource;
    return ret;
}


- (IBAction)pageSliderChanged:(id)sender {
    int cover = floor( self.pageSlider.value + 0.5 );

    if ( cover != self.flowCoverView.offset ) {
        self.flowCoverView.offset = cover;
        [self.flowCoverView draw];
        
//        self.page = cover;
		_documentContext.currentPage = cover;
        [self setLabels:cover];
    }
}

- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation {
	ThumbnailViewController *c = [ThumbnailViewController createViewController:orientation datasource:_datasource];
	c.documentContext = _documentContext;
//	c.documentId = self.documentId;
//	c.page = self.page;
	return c;
}

#pragma mark FlowCoverViewDelegate

- (int)flowCoverNumberImages:(FlowCoverView *)view {
	return [_documentContext totalPage];//[_datasource pages:documentId];
}

- (UIImage *)flowCover:(FlowCoverView *)view cover:(int)cover {
////////////////////////////

	return [_documentContext thumbnailWithIndex:cover];
//	return [_datasource thumbnail:_documentContext.documentId cover:cover];
}

- (void)flowCover:(FlowCoverView *)view didSelect:(int)cover {
	_documentContext.currentPage = cover;
	[parent showImage:_documentContext];
//    [parent showImage:documentId page:cover];
}

- (void)flowCover:(FlowCoverView *)view changeCurrent:(int)cover {
//    self.page = cover;
	_documentContext.currentPage = cover;
    [self setLabels:cover];
    
    [self.pageSlider setValue:cover animated:YES];
}

- (void)setLabels:(int)index {
	self.titleLabel.text = [_documentContext titleWithIndex:index];
//	self.titleLabel.text = [_datasource toc:documentId page:index].text;
    self.pageLabel.text = [NSString stringWithFormat:@"%d / %d" , index + 1 , [_documentContext totalPage]];
}

- (void)timerTicked:(NSTimer *)_timer {
    int cover = floor( self.pageSlider.value + 0.5 );
    
    if ( cover != self.flowCoverView.offset ) {
        self.flowCoverView.offset = cover;
        [self.flowCoverView draw];
        
//        self.page = cover;
		_documentContext.currentPage = cover;
        [self setLabels:cover];
    }
}
/*
- (void)setDocumentContext:(DocumentContext *)dc
{
	[_documentContext release];
	_documentContext = [dc retain];

	
}
*/

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.flowCoverView.delegate = self;
    
//    self.flowCoverView.offset = page;
    self.flowCoverView.offset = _documentContext.currentPage;
    [self.flowCoverView draw];
    
    self.pageSlider.minimumValue = 0.0;
    self.pageSlider.maximumValue = [_documentContext totalPage] - 1.0;
    self.pageSlider.value = _documentContext.currentPage;

    [self setLabels:_documentContext.currentPage];
}

- (void)dealloc {
    [flowCoverView release];
    [titleLabel release];
    [pageLabel release];
    [pageSlider release];
	[_datasource release];
	[_documentContext release];
//	[documentId release];
    
    [super dealloc];
}

@end
