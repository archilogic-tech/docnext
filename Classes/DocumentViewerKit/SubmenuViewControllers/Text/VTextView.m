//
//  VTextView.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/28.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "VTextView.h"
#import "NSString+Char.h"
#import "DocumentViewerConst.h"
#import "RubyMarker.h"
#import "TextSizeMarker.h"
#import "RubyRangeObject.h"

@interface VTextView ()
- (void) drawText:(CGRect)rect;
@end

@implementation VTextView

+ (float)getFactor:(NSArray *)textSizes line:(int)line {
    for ( TextSizeMarker *textSize in textSizes ) {
        if ( textSize.line == line ) {
            return textSize.factor;
        }
    }
    
    return 1.0;
}

+ (void)procChar:(NSString *)c font:(UIFont *)font point:(CGPoint)p width:(float)width {
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    BOOL needRestore = NO;

    if ( [VTextViewRotateRight rangeOfString:c].location != NSNotFound ) {
        needRestore = YES;
        CGContextSaveGState( ctx );
        
        CGAffineTransform t = CGAffineTransformTranslate( CGAffineTransformMakeRotation( M_PI / 2 ) , p.y - p.x - width / 5 , -p.x - p.y - width * 4 / 5 );
        CGContextConcatCTM(ctx, t);
    }
    
    if ( [VTextViewTransUpRight rangeOfString:c].location != NSNotFound ) {
        needRestore = YES;
        CGContextSaveGState( ctx );
        
        CGAffineTransform t = CGAffineTransformMakeTranslation( width * 1 / 2 , -width * 1 / 2 );
        CGContextConcatCTM(ctx, t);
    }
    
    // [ c drawAtPoint:p withFont:font ];
    [ c drawAtPoint:CGPointMake( p.x + ( width - [ c sizeWithFont:font ].width ) / 2 , p.y ) withFont:font ];
    
    if ( needRestore ) {
        CGContextRestoreGState( ctx );
    }
}

+ (void)procSingleRuby:(NSString *)ruby font:(UIFont *)font start:(CGPoint)start length:(float)length {
    start.y += ( length - font.pointSize * [ ruby length ] ) / 2;
    for ( int index = 0 ; index < [ ruby length ] ; index++ ) {
        [[ruby charAt:index] drawAtPoint:CGPointMake( start.x , start.y + font.pointSize * index ) withFont:font ];
    }
}

+ (void)procRuby:(NSString *)ruby rubyRanges:(NSArray *)rubyRanges font:(UIFont *)font {
    float totalLength = 0.0;
    for ( RubyRangeObject *rr in rubyRanges ) {
        totalLength += rr.end - rr.begin;
    }
    
    int nUsed = 0;
    for ( RubyRangeObject *rr in rubyRanges ) {
        int nChar = rr != [rubyRanges lastObject] ? (int)( [ruby length] / totalLength * (rr.end - rr.begin) ) : ( [ruby length] - nUsed );
        [VTextView procSingleRuby:[[ruby substringFromIndex:nUsed] substringToIndex:nChar] font:font start:CGPointMake(rr.base, rr.begin) length:( rr.end - rr.begin )];
        nUsed += nChar;
    }
}

+ (float)proc:(NSString *)text rubys:(NSArray *)rubys textSizes:(NSArray *)textSizes config:(TextViewConfig *)config
       height:(float)height startX:(float)startX performDraw:(BOOL)performDraw {
    int line = 0;
    float factor = [self getFactor:textSizes line:line];
    
    UIFont *font = [UIFont fontWithName:config.fontName size:config.fontSize * factor];
    UIFont *rubyFont = [UIFont fontWithName:config.fontName size:config.fontSize / 2 * factor];
    float fontHeight = font.leading;
    float rubyHeight = config.rubyEnable ? rubyFont.leading : 0;
    
    NSMutableArray *rubyRanges = nil;
    
    float x = startX - config.paddingRight - rubyHeight - fontHeight;
    float y = config.paddingTop;
    for ( int index = 0 ; index < [text length] ; index++ ) {
        unichar c = [text characterAtIndex:index];
        NSString *cs = [NSString stringWithChar:c];
        
        if ( c == '\n' ) {
            line++;
            
            y = config.paddingTop;
            
            float factor = [self getFactor:textSizes line:line];
            
            font = [font fontWithSize:config.fontSize * factor];
            rubyFont = [rubyFont fontWithSize:config.fontSize / 2 * factor];
            fontHeight = [text sizeWithFont:font].height;
            rubyHeight = config.rubyEnable ? [text sizeWithFont:rubyFont].height : 0;

            x -= config.lineSpacing + rubyHeight + fontHeight;

            continue;
        }
        
        if ( y + fontHeight >= height - config.paddingBottom ) {
            if ( [TextViewerWrap rangeOfString:cs].location == NSNotFound ) {
                if ( rubyRanges ) {
                    ((RubyRangeObject *)[rubyRanges lastObject]).end = y;
                }
                
                x -= config.lineSpacing + rubyHeight + fontHeight;
                y = config.paddingTop;
                
                if ( rubyRanges ) {
                    [rubyRanges addObject:[RubyRangeObject objectWithData:(x + fontHeight) begin:y end:0]];
                }
            }
        }
        
        if ( performDraw ) {
            [VTextView procChar:cs font:font point:CGPointMake( x , y ) width:fontHeight];
            
            for ( RubyMarker *ruby in rubys ) {
                if ( ruby.range.location == index ) {
                    rubyRanges = [NSMutableArray arrayWithCapacity:0];
                    [rubyRanges addObject:[RubyRangeObject objectWithData:(x + fontHeight) begin:y end:0]];
                }
                if ( ruby.range.location + ruby.range.length - 1 == index ) {
                    ((RubyRangeObject *)[rubyRanges lastObject]).end = y + fontHeight;
                    
                    [VTextView procRuby:ruby.text rubyRanges:rubyRanges font:rubyFont];

                    rubyRanges = nil;
                }
            }
        }
        
        y += fontHeight;
    }
    
    return -( x - config.paddingLeft );
}

+ (float)measureWidth:(NSString *)text textSizes:(NSArray *)textSizes baseFontSize:(float)baseFontSize height:(float)height {
    TextViewConfig *config = [TextViewConfig configVDefault];
    config.fontSize = baseFontSize;
    
    return [VTextView proc:text rubys:nil textSizes:textSizes config:config height:height startX:0 performDraw:NO];
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
    
    [VTextView proc:self.text rubys:self.rubys textSizes:self.textSizes config:self.config height:rect.size.height
             startX:rect.size.width performDraw:YES];
}

- (id)initWithFrame:(CGRect)frame {
    if ( ( self = [super initWithFrame:frame] ) ) {
        self.text = @"";
        self.config = [TextViewConfig configVDefault];
    }
    
    return self;
}

- (void)drawRect:(CGRect)rect {
    [self clear:rect];
    
    [self drawText:rect];
}

@end
