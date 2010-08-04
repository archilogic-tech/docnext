//
//  ImageViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapDocViewController.h"
#import "TiledScrollView.h"
#import "SeparationHolder.h"
#import "UITouchAwareWindow.h"
#import "MarkerView.h"
#import "TapDetector.h"
#import "UIScaleButton.h"
#import "OverlayManager.h"

@class ImageSearchViewController;

@interface ImageViewController : IUIViewController <TiledScrollViewDataSource, TapDetectorDelegate> {
    UIView *configView;
    UILabel *titleLabel;
    UIView *tiledScrollViewContainer;
    
    TiledScrollView *tiledScrollView;
    TiledScrollView *prevTiledScrollView;

    MarkerView *markerView;
    UIView *balloonContainerView;
    UITouchAwareWindow *window;

    UIPopoverController *popover;
    ImageSearchViewController *searchViewController;

    TapDetector *tapDetector;
    OverlayManager *overlayManager;
    
    int documentId;
    int currentIndex;
    int totalPage;
    NSArray *singlePageInfo;
    NSArray *pageHeads;
    NSArray *isSinglePage;

    NSOperationQueue *imageFetchQueue;    
}

@property(nonatomic,retain) IBOutlet UIView *configView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UIView *tiledScrollViewContainer;
@property(nonatomic,assign) UITouchAwareWindow *window;
@property(nonatomic) int documentId;

+ (ImageViewController *)createViewController:(int)documentId page:(int)page;

- (IBAction)homeButtonClick:(id)sender;
- (IBAction)tocViewButtonClick:(id)sender;
- (IBAction)thumbnailViewButtonClick:(id)sender;
- (IBAction)bookmarkViewButtonClick:(id)sender;
- (IBAction)textViewButtonClick:(id)sender;
- (IBAction)tweetButtonClick:(id)sender;
- (IBAction)searchButtonClick:(id)sender;
- (void)setIndexByPage:(int)page;
- (void)selectSearchResult:(int)page ranges:(NSArray *)ranges selectedIndex:(int)selectedIndex;
- (void)cancelSearch;

@end
