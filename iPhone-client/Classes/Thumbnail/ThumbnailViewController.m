//
//  ThumbnailViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/29.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ThumbnailViewController.h"
#import "FileUtil.h"

@interface ThumbnailViewController ()
- (void)setLabels:(int)index;
@end

@implementation ThumbnailViewController

@synthesize flowCoverView;
@synthesize titleLabel;
@synthesize pageLabel;
@synthesize pageSlider;
@synthesize documentId;
@synthesize page;

+ (ThumbnailViewController *)createViewController:(UIInterfaceOrientation)orientation docId:(int)documentId page:(int)page {
    ThumbnailViewController *ret = [[[ThumbnailViewController alloc] initWithNibName:
                                     [IUIViewController buildNibName:@"Thumbnail" orientation:orientation] bundle:nil] autorelease];
    [ret setLandspace:orientation];
    ret.documentId = documentId;
    ret.page = page;
    return ret;
}

- (IBAction)pageSliderChanged:(id)sender {
    int cover = floor( self.pageSlider.value + 0.5 );
    
    if ( cover != self.flowCoverView.offset ) {
        self.flowCoverView.offset = cover;
        [self.flowCoverView draw];
        
        self.page = cover;
        [self setLabels:cover];
    }
}

- (IUIViewController *)createViewController:(UIInterfaceOrientation)orientation {
    return [ThumbnailViewController createViewController:orientation docId:self.documentId page:self.page];
}

#pragma mark FlowCoverViewDelegate

- (int)flowCoverNumberImages:(FlowCoverView *)view {
    return [FileUtil pages:documentId];
}

- (UIImage *)flowCover:(FlowCoverView *)view cover:(int)cover {
    return [UIImage imageWithContentsOfFile:[FileUtil getFullPath:[NSString stringWithFormat:@"%d/images/thumb-%d.jpg" , documentId , [FileUtil pages:documentId] - 1 - cover]]];
}

- (void)flowCover:(FlowCoverView *)view didSelect:(int)cover {
    [parent showImage:documentId page:[FileUtil pages:documentId] - cover - 1];
}

- (void)flowCover:(FlowCoverView *)view changeCurrent:(int)cover {
    self.page = cover;
    [self setLabels:cover];
    
    [self.pageSlider setValue:cover animated:YES];
}

- (void)setLabels:(int)index {
    self.titleLabel.text = [FileUtil toc:documentId page:index].text;
    self.pageLabel.text = [NSString stringWithFormat:@"%d / %d" , [FileUtil pages:documentId] - index , [FileUtil pages:documentId]];
}

- (void)timerTicked:(NSTimer *)_timer {
    int cover = floor( self.pageSlider.value + 0.5 );
    
    if ( cover != self.flowCoverView.offset ) {
        self.flowCoverView.offset = cover;
        [self.flowCoverView draw];
        
        self.page = cover;
        [self setLabels:cover];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.flowCoverView.delegate = self;
    
    self.flowCoverView.offset = [FileUtil pages:documentId] - page - 1;
    [self.flowCoverView draw];
    
    self.pageSlider.minimumValue = 0.0;
    self.pageSlider.maximumValue = [FileUtil pages:documentId] - 1.0;
    self.pageSlider.value = [FileUtil pages:documentId] - page;
    
    [self setLabels:[FileUtil pages:documentId] - page - 1];
}

- (void)dealloc {
    [flowCoverView release];
    [titleLabel release];
    [pageLabel release];
    [pageSlider release];
    
    [super dealloc];
}

@end
