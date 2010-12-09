//
//  MarkerView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIHighlightIndicator.h"
#import "UIURILinkIndicator.h";
#import "UIGoToPageLinkIndicator.h"

@interface MarkerView : UIView {
    NSMutableSet *searchResults;
    NSMutableArray *selections;
    NSMutableDictionary *highlights;
}

// Take care atIndex is different from removal
- (void)addSelectionMarker:(CGRect)rect atIndex:(int)atIndex;
- (void)removeSelectionMarker:(int)atIndex;
- (void)clearSelectionMarker;
- (void)addSearchResultMarker:(CGRect)rect selected:(BOOL)selected;
- (void)clearSearchResultMarker;
- (UIHighlightIndicator *)addHighlightMarker:(CGRect)rect color:(UIColor *)color serial:(int)serial;
- (int)getHighlightNextSerial;
- (void)setHighlightSelected:(int)serial;
- (void)changeHighlightColor:(int)serial color:(UIColor *)color;
- (void)deleteHighlight:(int)serial;
- (CGPoint)calcHighlightTip:(int)serial;
- (UIURILinkIndicator *)addURILink:(CGRect)rect;
- (UIGoToPageLinkIndicator *)addGoToPageLink:(CGRect)rect;

@end
