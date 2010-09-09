//
//  MarkerView.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/08/02.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

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
- (UIControl *)addHighlightMarker:(CGRect)rect color:(UIColor *)color serial:(int)serial;
- (int)getHighlightNextSerial;
- (void)setHighlightSelected:(int)serial;
- (void)changeHighlightColor:(int)serial color:(UIColor *)color;
- (void)deleteHighlight:(int)serial;

@end
