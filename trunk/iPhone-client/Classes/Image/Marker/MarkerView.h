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
}

- (void)addSearchResultMarker:(CGRect)rect selected:(BOOL)selected;
// Take care atIndex is different from removal
- (void)addSelectionMarker:(CGRect)rect atIndex:(int)atIndex;
- (void)removeSelectionMarker:(int)atIndex;
- (void)clearSearchResultMarker;
- (void)clearSelectionMarker;

@end
