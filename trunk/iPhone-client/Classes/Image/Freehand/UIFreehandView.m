//
//  UIFreehandView.m
//  FreehandDrawing
//
//  Created by Yoskaku Toyama on 10/09/17.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "UIFreehandView.h"
#import "ObjPoint.h"
#import "ObjBezier.h"

#define DISTANCE_THRESHOLD (200)
#define TIME_THRESHOLD (0.05)

@interface UIFreehandView (private)
- (CGContextRef)createOffscreenContext:(void *)data size:(CGSize)size;
@end

@implementation UIFreehandView

@synthesize points = _points;
@synthesize enabled = _enabled;
@synthesize delegate = _delegate;

- (void)initialize {
    self.backgroundColor = [UIColor clearColor];
    self.clearsContextBeforeDrawing = NO;
    self.userInteractionEnabled = NO;
    
    _points = [[NSMutableArray arrayWithCapacity:0] retain];
    _bufferContext = [self createOffscreenContext:nil size:self.frame.size];
}

- (id)initWithFrame:(CGRect)frame {
    if ((self = [super initWithFrame:frame])) {
        [self initialize];
    }
    
    return self;
}

- (id)initWithCoder:(NSCoder *)decoder {
    if ((self = [super initWithCoder:decoder])) {
        [self initialize];
    }
    
    return self;
}

- (void)dealloc {
    CGContextRelease(_bufferContext);
    
    [_points release];
    
    [super dealloc];
}

- (CGContextRef)createOffscreenContext:(void *)data size:(CGSize)size  {
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context = CGBitmapContextCreate(data, size.width, size.height, 8, size.width * 4, colorSpace, kCGImageAlphaPremultipliedLast);
    CGColorSpaceRelease(colorSpace);
    
    return context;
}

- (void)addPoint:(NSSet *)touches {
    [[_points lastObject] addObject:[ObjPoint pointFromCGPoint:[[touches anyObject] locationInView:self]]];
    
    [self setNeedsDisplay];
}

- (ObjBezier *)calcBezier:(CGPoint)p0 p1:(CGPoint)p1 p2:(CGPoint)p2 p3:(CGPoint)p3 {
    double k = 0.3;
    
    ObjBezier *ret = [[ObjBezier new] autorelease];
    
    ret.p0 = p1;
    ret.p1 = p2;
    
    double r = k * hypot(p1.x - p2.x, p1.y - p2.y);
    
    double dx = p2.x - p0.x;
    double dy = p2.y - p0.y;
    double b = hypot(dx, dy);
    ret.cp0 = CGPointMake(p1.x + r * dx / b, p1.y + r * dy / b);
    
    dx = p1.x - p3.x;
    dy = p1.y - p3.y;
    b = hypot(dx, dy);
    ret.cp1 = CGPointMake(p2.x + r * dx / b, p2.y + r * dy / b);
    
    return ret;
}

- (void)drawLines:(CGContextRef)c {
    for ( NSArray *points in _points ) {
        BOOL first = YES;
        for ( ObjPoint *point in points ) {
            if ( first ) {
                first = NO;
                
                CGContextMoveToPoint(c, point.x, point.y);
            } else {
                CGContextAddLineToPoint(c, point.x, point.y);
            }
        }
    }
}

- (CGPoint)calcBezierPoint:(ObjBezier *)bezier t:(double)t {
    double v = 1.0 - t;
    return CGPointMake(v*v*v*bezier.p0.x + 3*v*v*t*bezier.cp0.x + 3*v*t*t*bezier.cp1.x + t*t*t*bezier.p1.x,
                       v*v*v*bezier.p0.y + 3*v*v*t*bezier.cp0.y + 3*v*t*t*bezier.cp1.y + t*t*t*bezier.p1.y);
}

- (void)drawBezier:(CGContextRef)c bezier:(ObjBezier *)bezier
         widthFrom:(double)widthFrom width:(double)width widthTo:(double)widthTo {
    if ( YES ) {
        double dist = hypot(bezier.p0.x - bezier.p1.x, bezier.p0.y - bezier.p1.y);
        int step = dist;
        for ( int index = 0 ; index < step + 1 ; index++ ) {
            double t0 = index / dist;
            double t1 = index < step ? (index + 1) / dist : 1.0;
            double w = t0 < 0.5 ? (t0 * 2 * (width - widthFrom) + widthFrom) : ((t0 - 0.5) * 2 * (widthTo - width) + width);
            
            CGPoint from = [self calcBezierPoint:bezier t:t0];
            CGPoint to = [self calcBezierPoint:bezier t:t1];
            
            CGContextSetLineCap(c, kCGLineCapRound);
            
            CGContextSetLineWidth(c, w);
            CGContextMoveToPoint(c, from.x, from.y);
            CGContextAddLineToPoint(c, to.x, to.y);
            CGContextStrokePath(c);
        }
    }else{
        // pefromace research
        CGContextSetLineCap(c, kCGLineCapRound);
        
        CGContextSetLineWidth(c, width);
        CGContextMoveToPoint(c, bezier.p0.x, bezier.p0.y);
        CGContextAddCurveToPoint(c, bezier.cp0.x, bezier.cp0.y, bezier.cp1.x, bezier.cp1.y, bezier.p1.x, bezier.p1.y);
        CGContextStrokePath(c);
    }
}

- (double)calcLineWidth:(CGPoint)from to:(CGPoint)to {
    double dist = pow( from.x - to.x , 2 ) + ( from.y - to.y , 2 );
    return pow( 0.1 , MIN( dist / 10000.0 , 1 ) ) * 10.0;
}

- (void)drawPoint:(CGContextRef)c points:(NSArray *)points {
    CGPoint point = [[points objectAtIndex:0] toCGPoint];
    
    CGContextSetLineCap(c, kCGLineCapRound);
    
    CGContextSetLineWidth(c, 10.0);
    CGContextMoveToPoint(c, point.x, point.y);
    CGContextAddLineToPoint(c, point.x, point.y);
    CGContextStrokePath(c);
}

- (void)drawBezierWithIndex:(CGContextRef)c index:(int)index points:(NSArray *)points {
    int headDelta = index - 1 < 0 ? 1 : 0;
    int tailDelta = index + 2 < points.count ? 0 : 1;
    
    double widthFrom = [self calcLineWidth:[[points objectAtIndex:(index - 1 + headDelta)] toCGPoint]
                                        to:[[points objectAtIndex:(index + headDelta)] toCGPoint]];
    double widthTo = [self calcLineWidth:[[points objectAtIndex:(index + 1 - tailDelta)] toCGPoint]
                                      to:[[points objectAtIndex:(index + 2 - tailDelta)] toCGPoint]];
    double width = [self calcLineWidth:[[points objectAtIndex:index] toCGPoint]
                                    to:[[points objectAtIndex:(index + 1)] toCGPoint]];
    ObjBezier *bezier = [self calcBezier:[[points objectAtIndex:(index - 1 + headDelta)] toCGPoint]
                                      p1:[[points objectAtIndex:index] toCGPoint]
                                      p2:[[points objectAtIndex:(index + 1)] toCGPoint]
                                      p3:[[points objectAtIndex:(index + 2 - tailDelta)] toCGPoint]];
    
    [self drawBezier:c bezier:bezier widthFrom:((widthFrom + width) / 2.0) width:width widthTo:((widthTo + width) / 2.0)];
}

- (void)drawBezierWithState:(CGContextRef)c drawLast:(BOOL)drawLast {
    if ( _points.count == 0 ) {
        return;
    }

    NSArray *points = [_points lastObject];
    
    if ( drawLast ) {
        if ( points.count >= 2 ) {
            [self drawBezierWithIndex:c index:(points.count - 2) points:points];
        } else {
            [self drawPoint:c points:points];
        }
    } else {
        if ( points.count >= 3 ) {
            [self drawBezierWithIndex:c index:(points.count - 3) points:points];
        }
    }
}

- (void)drawRect:(CGRect)rect {
    [self drawBezierWithState:_bufferContext drawLast:NO];

    CGImageRef image = CGBitmapContextCreateImage(_bufferContext); 
    
    CGContextRef c = UIGraphicsGetCurrentContext();
    CGContextDrawImage(c, rect, image);
    
    CGImageRelease(image);
    
    [self drawBezierWithState:c drawLast:YES];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [_points addObject:[NSMutableArray arrayWithCapacity:0]];
    
    [self addPoint:touches];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    [self addPoint:touches];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    [self drawBezierWithState:_bufferContext drawLast:YES];
    
    [_delegate pointsDidChange:self];
}

- (void)redrawBuffer {
    CGContextClearRect(_bufferContext, self.frame);
    
    for ( NSMutableArray *stroke in _points ) {
        if ( stroke.count > 1 ) {
            for ( int index = 0 ; index < stroke.count - 1 ; index++ ) {
                [self drawBezierWithIndex:_bufferContext index:index points:stroke];
            }
        } else {
            [self drawPoint:_bufferContext points:stroke];
        }
    }
}

#pragma mark public

- (void)undo {
    if ( _points.count > 0 ) {
        [_points removeLastObject];
    
        [self redrawBuffer];
        [self setNeedsDisplay];
    }
}

- (void)clear {
    if ( _points.count > 0 ) {
        [_points removeAllObjects];

        [self redrawBuffer];
        [self setNeedsDisplay];
    }
}

- (void)loadPoints:(NSArray *)points {
    NSAutoreleasePool *pool = [NSAutoreleasePool new];
    
    [_points removeAllObjects];
    
    for ( NSArray *stroke in points ) {
        NSMutableArray *strokeBuf = [NSMutableArray arrayWithCapacity:0];
        for ( ObjPoint *point in stroke ) {
            [strokeBuf addObject:point];
        }
        [_points addObject:strokeBuf];
    }
    
    [self redrawBuffer];
    [self setNeedsDisplay];
    
    [pool release];
}

@end
