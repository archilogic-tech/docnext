//
//  MapDocViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TiledScrollView.h"
#import "IUIViewController.h"
#import "UITouchAwareWindow.h"
#import "DocumentViewerDatasource.h"

@interface MapDocViewController : UIViewController {
    IUIViewController *current;
    UITouchAwareWindow *window;
    UIInterfaceOrientation willInterfaceOrientation;
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) IUIViewController *current;
@property(nonatomic,assign) UITouchAwareWindow *window;
@property(nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;


- (void)showHome:(BOOL)animated;
- (void)showBookshelfDeletion;
- (void)showImage:(id)documentId page:(int)page;
- (void)showTOC:(id)documentId prevPage:(int)prevPage;
- (void)showThumbnail:(id)documentId page:(int)page;
- (void)showBookmark:(id)documentId page:(int)page;
- (void)showText:(id)documentId page:(int)page;

@end
