//
//  TextViewController.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "TextViewController.h"
#import "HTextView.h"
#import "VTextView.h"
#import "RubyMarker.h"
#import "TextSizeMarker.h"
#import "NSString+Data.h"
#import "TOCObject.h"
#import "MarkupParser.h"

@interface TextViewController ()
- (void)movePage:(BOOL)isNext;
- (void)addConfiguredTextView:(NSString *)text rubys:(NSArray *)rubys textSizes:(NSArray *)textSizes;
- (void)loadCurrent;
@end

@implementation TextViewController

@synthesize titleLabel;
@synthesize configView;
@synthesize fontSizeSegment;
@synthesize colorSegment;
@synthesize directionSegment;
@synthesize scrollViewHolder;
@synthesize current;
@synthesize prev;

@synthesize documentContext = _documentContext;

#pragma mark public

+ (TextViewController *)createViewController
{
	UIInterfaceOrientation o = [UIDevice currentDevice].orientation;
    TextViewController *ret = [[[TextViewController alloc] initWithNibName:
                                [Util buildNibName:@"Text" orientation:o] bundle:nil] autorelease];
    return ret;
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
	return YES;
}

- (void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	[super didRotateFromInterfaceOrientation:fromInterfaceOrientation];

    // TODO willRotateに移す
	[self loadCurrent];
}



- (IBAction)imageViewButtonClick:(id)sender {
	[self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)toggleConfigViewButtonClick:(id)sender {
    double dst = self.configView.alpha > 0 ? 0 : 1;
    
	[UIView beginAnimations:nil context:nil];
	[UIView setAnimationDuration:0.3];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationTransition:UIViewAnimationTransitionNone forView:self.configView cache:NO];

    self.configView.alpha = dst;

    [UIView commitAnimations];
}

- (IBAction)configSegmentChange:(id)sender {
    [self loadCurrent];
    [self toggleConfigViewButtonClick:nil];
}

#pragma mark private

- (void)movePage:(BOOL)isNext {
    if ( isNext ? _documentContext.currentPage + 1 >= [_documentContext totalPage] : _documentContext.currentPage - 1 < 0 ) {
        return;
    }
    
    _documentContext.currentPage += isNext ? 1 : -1;
    
    self.titleLabel.text = @"Loading...";
    [self addConfiguredTextView:@"" rubys:[NSArray array] textSizes:[NSArray array]];

    CATransition *animation = [CATransition animation];
    animation.duration = 0.5;
    animation.type = kCATransitionPush;
    
    BOOL isHorizontal = self.directionSegment.selectedSegmentIndex == 0;
    animation.subtype = isHorizontal ? ( isNext ? kCATransitionFromTop : kCATransitionFromBottom ) : ( isNext ? kCATransitionFromLeft : kCATransitionFromRight );
    
    animation.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    animation.delegate = self;
    
    [self.scrollViewHolder.layer addAnimation:animation forKey:nil];
}

- (void)animationDidStart:(CAAnimation *)theAnimation {
    [self loadCurrent];
}

- (int)getTextColor {
    switch ( self.colorSegment.selectedSegmentIndex ) {
        case 0:
            return 0x202020;
        case 1:
            return 0xf0f0f0;
    }
    
    return 0;
}

- (int)getBackgroundColor {
    switch ( self.colorSegment.selectedSegmentIndex ) {
        case 0:
            return 0xf0f0f0;
        case 1:
            return 0x202020;
    }
    
    return 0;
}

- (void)addConfiguredTextView:(NSString *)text rubys:(NSArray *)rubys textSizes:(NSArray *)textSizes {
    [self.current removeFromSuperview];

    self.prev = self.current;

	
	self.current = [[[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.scrollViewHolder.frame.size.width , self.scrollViewHolder.frame.size.height)] autorelease];
	self.current.autoresizesSubviews = YES;
    self.current.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin |
										UIViewAutoresizingFlexibleWidth |
										UIViewAutoresizingFlexibleRightMargin |
										UIViewAutoresizingFlexibleTopMargin |
										UIViewAutoresizingFlexibleHeight |
										UIViewAutoresizingFlexibleBottomMargin;
    
    self.current.delegate = self;
    self.current.indicatorStyle = ([self getBackgroundColor] & 0xff) > 0x80 ? UIScrollViewIndicatorStyleBlack : UIScrollViewIndicatorStyleWhite;
    
    float fontSize = 18 + ( self.fontSizeSegment.selectedSegmentIndex - 1 ) * 6;
    BOOL isHorizontal = self.directionSegment.selectedSegmentIndex == 0;
    
    CGSize contentSize = self.scrollViewHolder.frame.size;
    if ( isHorizontal ) {
        contentSize.height = MAX( contentSize.height + 1 ,
                                 [HTextView measureHeight:text textSizes:textSizes baseFontSize:fontSize
                                                    width:contentSize.width] );
    } else {
        contentSize.width = MAX( contentSize.width + 1 ,
                                [VTextView measureWidth:text textSizes:textSizes baseFontSize:fontSize
                                                 height:contentSize.height] );
    }
    
    self.current.contentSize = contentSize;
    self.current.contentOffset = CGPointMake( contentSize.width - self.scrollViewHolder.frame.size.width , 0 );

    ITextView *view;
    if ( isHorizontal ) {
        view = [HTextView alloc];
    } else {
        view = [VTextView alloc];
    }
    view = [[view initWithFrame:CGRectMake( 0, 0, contentSize.width, contentSize.height)] autorelease];
    view.text = text;
    view.rubys = rubys;
    view.textSizes = textSizes;
    view.config.fontSize = fontSize;
    view.config.textColor = [self getTextColor];
    view.config.backgroundColor = [self getBackgroundColor];
    
    [self.current addSubview:view];
    [self.scrollViewHolder addSubview:self.current];
}

#pragma mark load

- (void)loadCurrent {
    self.titleLabel.text = [_documentContext title];//  [_datasource toc:documentId page:currentPage].text;
    
	NSString *text = [_documentContext texts];
//    NSString *text = [_datasource texts:documentId page:currentPage];
    MarkupParseResult *result = [[[MarkupParser new] autorelease] parse:text];
    
    [self addConfiguredTextView:result.text rubys:result.rubys textSizes:result.textSizes];
}

#pragma mark UIScrollViewDelegate

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    BOOL isHorizontal = self.directionSegment.selectedSegmentIndex == 0;
    if ( isHorizontal ) {
        int threashold = scrollView.frame.size.height / 20;
        if ( scrollView.contentOffset.y < -threashold ) {
            [self movePage:NO];
        }
        if ( scrollView.contentSize.height - ( scrollView.contentOffset.y + scrollView.frame.size.height ) < -threashold ) {
            [self movePage:YES];
        }
    } else {
        int threashold = scrollView.frame.size.width / 20;
        if ( scrollView.contentOffset.x < -threashold ) {
            [self movePage:YES];
        }
        if ( scrollView.contentSize.width - ( scrollView.contentOffset.x + scrollView.frame.size.width ) < -threashold ) {
            [self movePage:NO];
        }
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
//    totalPage = [_datasource pages:documentId];
    
    [self loadCurrent];
}

- (void)dealloc {
    [titleLabel release];
    [configView release];
    [scrollViewHolder release];
    [current release];
	
//	[_datasource release];
	[_documentContext release];
//	[documentId release];
    [super dealloc];
}

@end
