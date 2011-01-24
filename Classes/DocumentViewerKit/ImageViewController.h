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

@class ImageSearchViewController;

typedef enum {
    ImageViewLinkModeURI,
    ImageViewLinkModeGoToPage
} ImageViewLinkMode;

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
	// UI系
    UIView *configView;
    UILabel *titleLabel;
    UISwitch *_freehandSwitch;
    UIView *tiledScrollViewContainer;
    UIView *selectionMenuView;
    UIView *highlightMenuView;
    UIView *highlightCommentMenuView;
    UITextField *highlightCommentTextField;
    
    TiledScrollView *tiledScrollView;
    TiledScrollView *prevTiledScrollView;

    MarkerView *markerView;
    UIFreehandView *_freehandView;
    UIView *balloonContainerView;

    UIPopoverController *popover;

    BOOL isIgnoreTap;

    OverlayManager *overlayManager;
    int currentHighlightSerial;

    NSMutableDictionary *highlights;
    
    ImageViewLinkMode linkMode;
    NSString *linkURI;
    int linkPage;

	DocumentContext *_documentContext;
	
	id<NSObject,DocumentViewerDatasource> _datasource;					
}

@property(nonatomic,retain) IBOutlet UIView *configView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UISwitch *freehandSwitch;
@property(nonatomic,retain) IBOutlet UIView *tiledScrollViewContainer;
@property(nonatomic,retain) IBOutlet UIView *selectionMenuView;
@property(nonatomic,retain) IBOutlet UIView *highlightMenuView;
@property(nonatomic,retain) IBOutlet UIView *highlightCommentMenuView;
@property(nonatomic,retain) IBOutlet UITextField *highlightCommentTextField;

@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;
@property (nonatomic, retain) DocumentContext *documentContext;


+ (ImageViewController *)createViewController:(id<NSObject,DocumentViewerDatasource>)datasource;

- (IBAction)homeButtonClick:(id)sender;
- (IBAction)tocViewButtonClick:(id)sender;
- (IBAction)thumbnailViewButtonClick:(id)sender;
- (IBAction)bookmarkViewButtonClick:(id)sender;
- (IBAction)textViewButtonClick:(id)sender;
- (IBAction)tweetButtonClick:(id)sender;
- (IBAction)searchButtonClick:(id)sender;
- (IBAction)freehandUndoClick;
- (IBAction)freehandClearClick;
- (IBAction)freehandSwitchChanged;
- (IBAction)copyButtonClick;
- (IBAction)highlightButtonClick;
- (IBAction)highlightCommentButtonClick;
- (IBAction)highlightCommentApplyButtonClick;
- (IBAction)highlightChangeColorClick:(UIButton *)sender;
- (IBAction)highlightDeleteClick;

@end
