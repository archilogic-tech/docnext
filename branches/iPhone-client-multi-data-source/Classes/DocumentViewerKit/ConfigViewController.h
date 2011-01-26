//
//  ConfigViewController.h
//  MapDoc
//
//  Created by sakukawa on 11/01/26.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "DocumentContext.h"
#import "DocumentViewerDatasource.h"
#import "ImageSearchViewController.h"

@class ImageViewController;

@interface ConfigViewController : UIViewController<ImageSearchDelegate> {
	IBOutlet UILabel *titleLabel;
    IBOutlet UISwitch *_freehandSwitch;

    UIPopoverController *popover;

	ImageViewController *_parent;
}

@property (nonatomic, assign) ImageViewController *parent;

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

@end
