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
#import "TapDetectingView.h"

@class ImageSearchViewController;

@interface ImageViewController : IUIViewController <TiledScrollViewDataSource, TapDetectingViewDelegate> {
    UIView *configView;
    UILabel *titleLabel;
    UIView *tiledScrollViewContainer;
    
    int documentId;

    TiledScrollView *tiledScrollView;
    TiledScrollView *prevTiledScrollView;
    UIPopoverController *popover;
    ImageSearchViewController *searchViewController;
    int currentIndex;
    int totalPage;
    NSArray *singlePageInfo;
    NSArray *pageHeads;
    NSArray *isSinglePage;
    int nRequestTile;
}

@property(nonatomic,retain) IBOutlet UIView *configView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UIView *tiledScrollViewContainer;
@property(nonatomic) int documentId;
@property(nonatomic,retain) TiledScrollView *tiledScrollView;

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
