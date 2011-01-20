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
#import "UIFreehandView.h"

#import "DocumentViewerDatasource.h"
#import "DocumentViewerDelegate.h"

#import "DocumentContext.h"

@class ImageSearchViewController;

typedef enum {
    ImageViewLinkModeURI,
    ImageViewLinkModeGoToPage
} ImageViewLinkMode;

/*!
    @class       ImageViewController 
    @superclass  IUIViewController <TiledScrollViewDataSource, TapDetectorDelegate, OverlayManagerDelegate, UIActionSheetDelegate, UIFreehandViewDelegate>
    @abstract    DocumentViewerのベースとなるクラス
    @discussion  +createViewController を呼ぶことで、制御がDocumentViewerに移る。
*/
@interface ImageViewController : IUIViewController <TiledScrollViewDataSource, TapDetectorDelegate,
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
    UITouchAwareWindow *window;

    UIPopoverController *popover;
    ImageSearchViewController *searchViewController;

    BOOL isIgnoreTap;

    TapDetector *tapDetector;
    OverlayManager *overlayManager;
    int currentHighlightSerial;

    NSMutableDictionary *highlights;
    
    ImageViewLinkMode linkMode;
    NSString *linkURI;
    int linkPage;

	// 現在表示している情報
	DocumentContext *_documentContext;
	
	
    // メタ情報系
	//    id<NSObject> documentId;
	//int currentDocumentIndex;
    //int currentIndex;

 //   int totalPage;
 //   NSArray *singlePageInfo;
 //   NSArray *pageHeads;
 //   NSArray *isSinglePage;

	id<NSObject,DocumentViewerDatasource> _datasource;					
	id<NSObject,DocumentViewerDelegate> _delegate;
}

@property(nonatomic,retain) IBOutlet UIView *configView;
@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UISwitch *freehandSwitch;
@property(nonatomic,retain) IBOutlet UIView *tiledScrollViewContainer;
@property(nonatomic,retain) IBOutlet UIView *selectionMenuView;
@property(nonatomic,retain) IBOutlet UIView *highlightMenuView;
@property(nonatomic,retain) IBOutlet UIView *highlightCommentMenuView;
@property(nonatomic,retain) IBOutlet UITextField *highlightCommentTextField;
@property(nonatomic,assign) UITouchAwareWindow *window;
//@property(nonatomic,copy) id<NSObject> documentId;

@property (nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;
@property (nonatomic, assign) id<NSObject,DocumentViewerDelegate> delegate;
@property (nonatomic, retain) DocumentContext *documentContext;


/*!
    @method     createViewController:datasource:window:
    @abstract   DocumentViewerを開始する
    @discussion 
    @param      orientation デバイスの向き
    @param      datasource データソース
    @param      window 親Window(windowを渡すのはやめる可能性あり)
    @result     生成されたImageViewControllerを返す
*/
+ (ImageViewController *)createViewController:(UIInterfaceOrientation)orientation
								   datasource:(id<NSObject,DocumentViewerDatasource>)datasource
                                       window:(UITouchAwareWindow *)window;
	

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


//- (void)setIndexByPage:(int)page;
- (void)selectSearchResult:(int)page ranges:(NSArray *)ranges selectedIndex:(int)selectedIndex;
- (void)cancelSearch;

@end
