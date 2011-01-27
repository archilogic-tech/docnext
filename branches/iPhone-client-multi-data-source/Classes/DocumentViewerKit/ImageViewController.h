//
//  ImageViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "TiledScrollView.h"
#import "SeparationHolder.h"
#import "MarkerView.h"
#import "UIScaleButton.h"
#import "OverlayManager.h"
#import "UIFreehandView.h"

#import "ImageSearchViewController.h"
#import "DocumentViewerDatasource.h"
#import "DocumentContext.h"

#import "ConfigViewController.h"

@class ImageSearchViewController;

typedef enum {
    ImageViewLinkModeURI,
    ImageViewLinkModeGoToPage
} ImageViewLinkMode;

typedef enum {
	PageTransitionNone,
	PageTransitionFromLeft,
	PageTransitionFromRight
} PageTransitionAnimationType ;


/*!
    @class       ImageViewController 
    @superclass  UIViewController <TiledScrollViewDataSource, ImageSearchDelegate, OverlayManagerDelegate, UIActionSheetDelegate, UIFreehandViewDelegate>
    @abstract    DocumentViewerのベースとなるクラス
    @discussion  +createViewController を呼ぶことで、制御がDocumentViewerに移る。
*/
@interface ImageViewController : UIViewController <TiledScrollViewDataSource, ImageSearchDelegate,
                                                    OverlayManagerDelegate, UIActionSheetDelegate,
                                                    UIFreehandViewDelegate>
{
	ConfigViewController *_configViewController;
	
	// UI系
    //UIView *configView;

    //UILabel *titleLabel;
    //UISwitch *_freehandSwitch;

    
	IBOutlet UIView *_tiledScrollViewContainer;

	TiledScrollView *tiledScrollView;
    TiledScrollView *prevTiledScrollView;

    IBOutlet UIView *selectionMenuView;
    IBOutlet UIView *highlightMenuView;
    IBOutlet UIView *highlightCommentMenuView;
    IBOutlet UITextField *highlightCommentTextField;

    MarkerView *markerView;
    UIFreehandView *_freehandView;
    UIView *balloonContainerView;

   // UIPopoverController *popover;

    BOOL isIgnoreTap;

    OverlayManager *overlayManager;
    int currentHighlightSerial;

    NSMutableDictionary *highlights;
    
    ImageViewLinkMode linkMode;
    NSString *linkURI;
    int linkPage;

	DocumentContext *_documentContext;
	
	id<NSObject,DocumentViewerDatasource> _datasource;					

	UINavigationController *_container;
	NSArray *_subviews;
}

@property (nonatomic, readonly) UIFreehandView *freehandView;
@property (nonatomic, retain) ConfigViewController *configViewController;
@property (nonatomic, readonly) OverlayManager *overlayManager;
@property (nonatomic, readonly) UIView *tiledScrollViewContainer;
@property (nonatomic, readonly) TiledScrollView *tiledScrollView;


@property(nonatomic,retain) UIView *selectionMenuView;
@property(nonatomic,retain) UIView *highlightMenuView;
@property(nonatomic,retain) UIView *highlightCommentMenuView;
@property(nonatomic,retain) UITextField *highlightCommentTextField;

@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;
@property (nonatomic, retain) DocumentContext *documentContext;


+ (ImageViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource;

- (void)show;

- (IBAction)copyButtonClick;
- (IBAction)highlightButtonClick;
- (IBAction)highlightCommentButtonClick;
- (IBAction)highlightCommentApplyButtonClick;
- (IBAction)highlightChangeColorClick:(UIButton *)sender;
- (IBAction)highlightDeleteClick;

@end
