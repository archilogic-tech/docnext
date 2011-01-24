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
#import "DocumentViewerDatasource.h"

@interface MapDocViewController : UINavigationController {
//    UIViewController *current;
    UIWindow *window;
    UIInterfaceOrientation willInterfaceOrientation;
	id<NSObject,DocumentViewerDatasource> _datasource;
}

@property(nonatomic,retain) UIViewController *current;
@property(nonatomic,assign) UIWindow *window;
@property(nonatomic, retain) id<NSObject,DocumentViewerDatasource> datasource;


- (void)showHome:(BOOL)animated;
- (void)showBookshelfDeletion;
//- (void)showImage:(id)documentId page:(int)page;
//- (void)showTOC:(id)documentId prevPage:(int)prevPage;
//- (void)showThumbnail:(id)documentId page:(int)page;
//- (void)showBookmark:(id)documentId page:(int)page;
//- (void)showText:(id)documentId page:(int)page;


- (void)showImage:(DocumentContext*)documentContext;
- (void)showTOC:(DocumentContext*)documentContext;
- (void)showThumbnail:(DocumentContext*)documentContext;
- (void)showBookmark:(DocumentContext*)documentContext;
- (void)showText:(DocumentContext*)documentContext;

@end
