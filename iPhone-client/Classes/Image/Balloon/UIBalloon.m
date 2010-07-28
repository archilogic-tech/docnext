//
//  UIBalloon.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/27.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "UIBalloon.h"

#define UIBalloonPaddingForShadow (10)
#define UIBalloonPaddingForTip (10)
#define UIBalloonBorderWidth (3)
#define UIBalloonPaddingForText (5)
#define UIBalloonMaxTextWidth (200)

@implementation UIBalloon

- (void)drawRoundRect:(CGRect)rect radius:(CGFloat)radius context:(CGContextRef)context {	
    CGFloat lx = CGRectGetMinX(rect);
    CGFloat cx = CGRectGetMidX(rect);
    CGFloat rx = CGRectGetMaxX(rect);
    CGFloat by = CGRectGetMinY(rect);
    CGFloat cy = CGRectGetMidY(rect);
    CGFloat ty = CGRectGetMaxY(rect);
	
    CGContextMoveToPoint(context, lx, cy);
    CGContextAddArcToPoint(context, lx, by, cx, by, radius);
    CGContextAddArcToPoint(context, rx, by, rx, cy, radius);
    CGContextAddArcToPoint(context, rx, ty, cx, ty, radius);
    CGContextAddArcToPoint(context, lx, ty, lx, cy, radius);
    CGContextClosePath(context);
    CGContextFillPath(context);
}

- (void)drawTriangle:(CGContextRef)c x0:(float)x0 y0:(float)y0 x1:(float)x1 y1:(float)y1 x2:(float)x2 y2:(float)y2 {
    CGContextMoveToPoint(c, x0, y0);
    CGContextAddLineToPoint(c, x1, y1);
    CGContextAddLineToPoint(c, x2, y2);
    CGContextAddLineToPoint(c, x0, y0);
    CGContextClosePath(c);
    CGContextFillPath(c);
}

- (void)drawRect:(CGRect)rect {
    CGContextRef c = UIGraphicsGetCurrentContext();
    
    CGContextClearRect(c, rect);
    
    float ps = UIBalloonPaddingForShadow;
    float pt = UIBalloonPaddingForTip;
    float b = UIBalloonBorderWidth;
    float x = rect.origin.x + ps + pt;
    float y = rect.origin.y + ps + pt;
    float w = rect.size.width - (ps + pt) * 2;
    float h = rect.size.height - (ps + pt) * 2;
    
    CGContextSaveGState(c);
    CGContextSetShadow(c, CGSizeMake(4.0f, 4.0f), 6.0f);
    
    CGContextSetRGBFillColor( c, 1, 0, 0, 1 );
    [self drawRoundRect:CGRectMake( x, y, w, h ) radius:5 context:c];
    [self drawTriangle:c x0:(x + w / 2) y0:(y + h + pt) x1:(x + w / 2 - pt) y1:(y + h) x2:(x + w / 2 + pt) y2:(y + h)];
    
    CGContextRestoreGState(c);
    
    x += b;
    y += b;
    w -= b * 2;
    h -= b * 2;
    
    CGContextSetRGBFillColor( c, 1, 1, 1, 1 );
    [self drawRoundRect:CGRectMake( x, y, w, h ) radius:5 context:c];
}

- (id)initWithFrame:(CGRect)frame {
    if ( ( self = [super initWithFrame:frame] ) ) {
        self.backgroundColor = [UIColor clearColor];
        //self.alpha = 0.5;

    }
    
    return self;
}

- (id)initWithText:(NSString *)text tip:(CGPoint)_tip {
    tip = _tip;
    
    if ( ( self = [super initWithFrame:CGRectZero] ) ) {
        self.backgroundColor = [UIColor clearColor];
        self.alpha = 0.8;
        
        UILabel *label = [[UILabel new] autorelease];
        label.backgroundColor = [UIColor clearColor];
        label.lineBreakMode = UILineBreakModeWordWrap;
        label.numberOfLines = 0;
        label.frame = CGRectMake(0, 0, UIBalloonMaxTextWidth, 0);
        label.text = text;
        [label sizeToFit];
        
        float space = UIBalloonPaddingForShadow + UIBalloonPaddingForTip + UIBalloonBorderWidth + UIBalloonPaddingForText;
        
        tipOffset = CGPointMake(label.frame.size.width / 2 + space, label.frame.size.height + space * 2 - UIBalloonPaddingForTip);
        self.frame = CGRectMake(_tip.x - tipOffset.x , _tip.y - tipOffset.y, label.frame.size.width + space * 2, label.frame.size.height + space * 2);
        
        label.frame = CGRectMake(space, space, label.frame.size.width, label.frame.size.height);
        [self addSubview:label];
    }
    
    return self;
}

- (void)adjustForTip:(float)scale {
    self.frame = CGRectMake(tip.x * scale - tipOffset.x, tip.y * scale - tipOffset.y, self.frame.size.width , self.frame.size.height);
}

- (void)dealloc {
    [super dealloc];
}

@end
