//
//  HTextView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/25.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "HTextView.h"
#import "NSString+Char.h"
#import "DocumentViewerConst.h"
#import "RubyMarker.h"
#import "TextSizeMarker.h"
#import "RubyRangeObject.h"

@interface HTextView ()
- (void) drawText:(CGRect)rect;
@end

@implementation HTextView

+ (float)getFactor:(NSArray *)textSizes line:(int)line {
    for ( TextSizeMarker *textSize in textSizes ) {
        if ( textSize.line == line ) {
            return textSize.factor;
        }
    }
    
    return 1.0;
}

+ (void)procSingleRuby:(NSString *)ruby font:(UIFont *)font start:(CGPoint)start length:(float)length {
    start.x += ( length - [ruby sizeWithFont:font].width ) / 2;
    [ruby drawAtPoint:start withFont:font];
}

+ (void)procRuby:(NSString *)ruby rubyRanges:(NSArray *)rubyRanges font:(UIFont *)font {
    float totalLength = 0.0;
    for ( RubyRangeObject *rr in rubyRanges ) {
        totalLength += rr.end - rr.begin;
    }
    
    int nUsed = 0;
    for ( RubyRangeObject *rr in rubyRanges ) {
        int nChar = rr != [rubyRanges lastObject] ? (int)( [ruby length] / totalLength * (rr.end - rr.begin) ) : ( [ruby length] - nUsed );
        [HTextView procSingleRuby:[[ruby substringFromIndex:nUsed] substringToIndex:nChar] font:font start:CGPointMake(rr.begin, rr.base) length:( rr.end - rr.begin )];
        nUsed += nChar;
    }
}

+ (float)proc:(NSString *)text rubys:(NSArray *)rubys textSizes:(NSArray *)textSizes config:(TextViewConfig *)config
        width:(float)width performDraw:(BOOL)performDraw {
    int line = 0;
    float factor = [self getFactor:textSizes line:line];
    
    UIFont *font = [UIFont fontWithName:config.fontName size:config.fontSize * factor];
    UIFont *rubyFont = [UIFont fontWithName:config.fontName size:config.fontSize / 2 * factor];
    float height = [text sizeWithFont:font].height;
    float rubyHeight = config.rubyEnable ? [text sizeWithFont:rubyFont].height : 0;
    
    NSMutableArray *rubyRanges = nil;
    
    float x = config.paddingLeft;
    float y = config.paddingTop + rubyHeight;
    for ( int index = 0 ; index < [text length] ; index++ ) {
        unichar c = [text characterAtIndex:index];
        NSString *cs = [NSString stringWithChar:c];
        
        if ( c == '\n' ) {
            line++;
            
            x = config.paddingLeft;
            y += height + config.lineSpacing;

            float factor = [self getFactor:textSizes line:line];
            
            font = [font fontWithSize:config.fontSize * factor];
            rubyFont = [rubyFont fontWithSize:config.fontSize / 2 * factor];
            height = [text sizeWithFont:font].height;
            rubyHeight = config.rubyEnable ? [text sizeWithFont:rubyFont].height : 0;
            
            y += rubyHeight;
            
            continue;
        }
        
        if ( x + [cs sizeWithFont:font].width >= width - config.paddingRight ) {
            if ( [TextViewerWrap rangeOfString:cs].location == NSNotFound ) {
                if ( rubyRanges ) {
                    ((RubyRangeObject *)[rubyRanges lastObject]).end = x;
                }

                x = config.paddingLeft;
                y += height + config.lineSpacing + rubyHeight;

                if ( rubyRanges ) {
                    [rubyRanges addObject:[RubyRangeObject objectWithData:(y - rubyHeight) begin:x end:0]];
                }
            }
        }
        
        float delta = [cs sizeWithFont:font].width;
        
        if ( performDraw ) {
            [cs drawAtPoint:CGPointMake( x , y ) withFont:font];
            
            for ( RubyMarker *ruby in rubys ) {
                if ( ruby.range.location == index ) {
                    rubyRanges = [NSMutableArray arrayWithCapacity:0];
                    // TODO rubyHeight / 5
                    [rubyRanges addObject:[RubyRangeObject objectWithData:(y - rubyHeight + rubyHeight / 5) begin:x end:0]];
                }
                if ( ruby.range.location + ruby.range.length - 1 == index ) {
                    ((RubyRangeObject *)[rubyRanges lastObject]).end = x + delta;
                    
                    [HTextView procRuby:ruby.text rubyRanges:rubyRanges font:rubyFont];
                    
                    rubyRanges = nil;
                }
            }
        }
        
        x += delta;
    }
    
    return y + height + config.paddingBottom;
}

+ (float)measureHeight:(NSString *)text textSizes:(NSArray *)textSizes baseFontSize:(float)baseFontSize width:(float)width {
    TextViewConfig *config = [TextViewConfig configHDefault];
    config.fontSize = baseFontSize;
    
    return [HTextView proc:text rubys:nil textSizes:textSizes config:config width:width performDraw:NO];
}

- (void)clear:(CGRect)rect {
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    
    CGContextClearRect( ctx , rect );
    
    CGContextSetRGBFillColor( ctx ,
                             ( ( config.backgroundColor >> 16 ) & 0xff ) / 255.0 ,
                             ( ( config.backgroundColor >> 8 ) & 0xff ) / 255.0 ,
                             ( config.backgroundColor & 0xff ) / 255.0 ,
                             1 );
    CGContextFillRect( ctx, rect );
}

- (void)drawText:(CGRect)rect {
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    CGContextSetRGBFillColor( ctx ,
                             ( ( config.textColor >> 16 ) & 0xff ) / 255.0 ,
                             ( ( config.textColor >> 8 ) & 0xff ) / 255.0 ,
                             ( config.textColor & 0xff ) / 255.0 ,
                             1 );
    
    [HTextView proc:self.text rubys:self.rubys textSizes:self.textSizes config:self.config width:rect.size.width performDraw:YES];
}

- (id)initWithFrame:(CGRect)frame {
    if ( ( self = [super initWithFrame:frame] ) ) {
        self.text = @"";
        self.config = [TextViewConfig configHDefault];
    }
    
    return self;
}

- (void)drawRect:(CGRect)rect {
    [self clear:rect];
    
    [self drawText:rect];
}

@end
