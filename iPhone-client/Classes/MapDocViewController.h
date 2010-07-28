//
//  MapDocViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TiledScrollView.h"
#import "TapDetectingView.h"
#import "IUIViewController.h"
//@class IUIViewController;

@interface MapDocViewController : UIViewController {
    IUIViewController *current;
}

@property(nonatomic,retain) IUIViewController *current;

- (void)showHome:(BOOL)animated;
- (void)showBookshelfDeletion;
- (void)showImage:(int)documentId page:(int)page;
- (void)showTOC:(int)documentId prevPage:(int)prevPage;
- (void)showThumbnail:(int)documentId page:(int)page;
- (void)showBookmark:(int)documentId page:(int)page;
- (void)showText:(int)documentId page:(int)page;

@end
