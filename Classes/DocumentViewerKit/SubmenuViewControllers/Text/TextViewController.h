//
//  TextViewController.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapDocViewController.h"
#import "ASIHTTPRequest.h"
#import "DocumentContext.h"

@interface TextViewController : IUIViewController <UIScrollViewDelegate> {
    UILabel *titleLabel;
    UIView *configView;
    UISegmentedControl *fontSizeSegment;
    UISegmentedControl *colorSegment;
    UISegmentedControl *directionSegment;
    UIView *scrollViewHolder;

//    id<NSObject> documentId;
//    int currentPage;
//    int totalPage;
//	id<NSObject,DocumentViewerDatasource> _datasource;

    UIScrollView *current;
    UIScrollView *prev;

	DocumentContext *_documentContext;
}

@property(nonatomic,retain) IBOutlet UILabel *titleLabel;
@property(nonatomic,retain) IBOutlet UIView *configView;
@property(nonatomic,retain) IBOutlet UISegmentedControl *fontSizeSegment;
@property(nonatomic,retain) IBOutlet UISegmentedControl *colorSegment;
@property(nonatomic,retain) IBOutlet UISegmentedControl *directionSegment;
@property(nonatomic,retain) IBOutlet UIView *scrollViewHolder;
@property(nonatomic,retain) UIScrollView *current;
@property(nonatomic,retain) UIScrollView *prev;

@property (nonatomic, retain) DocumentContext *documentContext;

+ (TextViewController *)createViewController:(UIInterfaceOrientation)orientation;


- (IBAction)imageViewButtonClick:(id)sender;
- (IBAction)toggleConfigViewButtonClick:(id)sender;
- (IBAction)configSegmentChange:(id)sender;

@end
