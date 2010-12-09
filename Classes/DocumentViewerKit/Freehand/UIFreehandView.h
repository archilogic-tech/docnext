//
//  UIFreehandView.h
//  FreehandDrawing
//
//  Created by Yoskaku Toyama on 10/09/17.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class UIFreehandView;

@protocol UIFreehandViewDelegate

- (void)pointsDidChange:(UIFreehandView *)sender;

@end


@interface UIFreehandView : UIView {
    NSMutableArray /* of NSMutableArray of ObjPoint */ *_points;
    CGContextRef _bufferContext;
    
    id<UIFreehandViewDelegate> _delegate;
}

@property(nonatomic,readonly) NSArray *points;
@property(nonatomic) BOOL enabled;
@property(nonatomic,assign) id<UIFreehandViewDelegate> delegate;

- (void)undo;
- (void)clear;
- (void)loadPoints:(NSArray *)points;

@end
