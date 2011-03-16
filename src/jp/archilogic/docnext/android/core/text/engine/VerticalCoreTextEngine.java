package jp.archilogic.docnext.android.core.text.engine;

import java.util.List;

import jp.archilogic.docnext.android.core.Size;
import jp.archilogic.docnext.android.core.text.CoreTextConfig;
import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.TextLayoutInfo;
import jp.archilogic.docnext.android.info.TextInfo;
import jp.archilogic.docnext.android.info.TextInfo.Dot;
import jp.archilogic.docnext.android.info.TextInfo.Ruby;
import jp.archilogic.docnext.android.info.TextInfo.TCY;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.common.collect.Lists;

public class VerticalCoreTextEngine implements CoreTextEngine {
    private void drawChar( final Canvas c , final Paint p , final String ch , final float x , final float y ,
            final float h , final boolean forceVertical ) {
        if ( !forceVertical && shouldRotate( ch ) ) {
            c.save();
            c.translate( h , 0 );
            c.rotate( 90 , x , y );

            c.drawText( ch , x , y + h , p );

            c.restore();
        } else {
            c.drawText( ch , x , y + h , p );
        }
    }

    @Override
    public void drawDots( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final TextInfo source ,
            final CoreTextConfig _config ) {
        p.setTextSize( _config.getRubyFontSize() );
        p.setColor( _config.defaultTextColor );

        final String dotChar = "﹅";

        for ( final Dot dot : source.dots ) {
            for ( int delta = 0 ; delta < dot.length ; delta++ ) {
                final TextLayoutInfo l = layouts[ dot.location + delta ];

                c.drawText( dotChar , l.lineMetrics - p.getTextSize() ,
                        l.y + p.getTextSize() + ( l.height - p.getTextSize() ) / 2 , p );
            }
        }
    }

    private void drawRuby( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final Ruby ruby ) {
        final float h = p.getTextSize() * ruby.text.length();

        final TextLayoutInfo from = layouts[ ruby.location ];
        final TextLayoutInfo to = layouts[ ruby.location + ruby.length - 1 ];

        final float textHeight = to.y + to.height - from.y;

        final float unit = textHeight > h ? ( textHeight - h ) / ruby.text.length() : 0;
        float y = from.y + ( unit > 0 ? unit / 2 : ( textHeight - h ) / 2 );

        for ( int index = 0 ; index < ruby.text.length() ; index++ ) {
            drawChar( c , p , ruby.text.substring( index , index + 1 ) , from.lineMetrics - p.getTextSize() , y ,
                    p.getTextSize() , false );

            y += p.getTextSize() + unit;
        }
    }

    @Override
    public void drawRubys( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final TextInfo source ,
            final CoreTextConfig _config ) {
        p.setTextSize( _config.getRubyFontSize() );
        p.setColor( _config.defaultTextColor );

        for ( final Ruby ruby : source.rubys ) {
            final List< Integer > splitHeadIndex = Lists.newArrayList();
            int currentLine = -1;
            for ( int delta = 0 ; delta < ruby.length ; delta++ ) {
                if ( layouts[ ruby.location + delta ].line != currentLine ) {
                    currentLine = layouts[ ruby.location + delta ].line;

                    splitHeadIndex.add( ruby.location + delta );
                }
            }

            int textPos = 0;
            for ( int index = 0 ; index < splitHeadIndex.size() ; index++ ) {
                final int from = splitHeadIndex.get( index );
                final int to = index + 1 < splitHeadIndex.size() ? //
                        splitHeadIndex.get( index + 1 ) : ruby.location + ruby.length;

                final int useTextCount = index + 1 < splitHeadIndex.size() ? //
                        Math.round( 1f * ruby.text.length() * ( to - from ) / ruby.length ) : //
                        ruby.text.length() - textPos;
                drawRuby( c , p , layouts , //
                        new Ruby( ruby.text.substring( textPos , textPos + useTextCount ) , from , to - from ) );

                textPos += useTextCount;
            }
        }
    }

    @Override
    public void drawText( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final TextInfo source ,
            final CoreTextConfig _config ) {
        c.drawColor( _config.backgroundColor );

        p.setTextSize( _config.fontSize );
        p.setColor( _config.defaultTextColor );

        for ( int index = 0 ; index < layouts.length ; index++ ) {
            final TextLayoutInfo l = layouts[ index ];

            if ( l != null ) {
                drawChar( c , p , source.at( index ) , l.x , l.y , l.height , isInTCY( source , index ) );
            }
        }
    }

    private PointF getCharOffset( final Paint p , final String ch ) {
        final boolean isHalf = "。、".contains( ch );
        final boolean isSmall = "ぁぃぅぇぉっゃゅょァィゥェォッャュョ".contains( ch );

        if ( isHalf ) {
            return new PointF( p.measureText( ch ) * 2 / 3 , p.getTextSize() / 2 );
        } else if ( isSmall ) {
            return new PointF( p.measureText( ch ) / 8 , p.getTextSize() / 8 );
        } else {
            return new PointF( 0 , 0 );
        }
    }

    private TCY getTCY( final TextInfo source , final int index ) {
        for ( final TCY tcy : source.tcys ) {
            if ( tcy.location == index ) {
                return tcy;
            }
        }

        return null;
    }

    private boolean isCharNotPermittedOnStart( final String ch ) {
        return "。、".contains( ch );
    }

    private boolean isInTCY( final TextInfo source , final int index ) {
        for ( final TCY tcy : source.tcys ) {
            if ( index >= tcy.location && index < tcy.location + tcy.length ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TextLayoutInfo[] layoutText( final Paint p , final TextInfo source , final CoreTextConfig config ,
            final Size surface ) {
        final TextLayoutInfo[] ret = new TextLayoutInfo[ source.length() ];

        float x = -config.horizontalPadding;
        float y = config.verticalPadding;

        final List< String > buffer = Lists.newArrayList();

        p.setTextSize( config.fontSize );

        int line = 0;

        for ( int index = 0 ; index < source.length() ; index++ ) {
            final String ch = source.at( index );

            final TCY tcy = getTCY( source , index );
            final float charHeight = tcy != null ? p.getTextSize() : measure( p , ch );
            boolean added = false;

            boolean isLineBreak = y + charHeight >= surface.height - config.verticalPadding;

            if ( isLineBreak ) {
                if ( config.lineBreakingRule == LineBreakingRule.SQUEEZE ) {
                    if ( isCharNotPermittedOnStart( ch ) ) {
                        y += charHeight;
                        buffer.add( ch );

                        added = true;
                    }
                }
            } else {
                if ( config.lineBreakingRule == LineBreakingRule.TO_NEXT ) {
                    if ( index + 1 < source.length() ) {
                        final String nextCh = source.at( index + 1 );

                        if ( isCharNotPermittedOnStart( nextCh ) && //
                                y + charHeight + measure( p , nextCh ) >= surface.height - config.verticalPadding ) {
                            isLineBreak = true;
                        }
                    }
                }
            }

            final boolean isLineFeed = ch.equals( "\n" ) || index + 1 >= source.length();
            if ( isLineFeed ) {
                if ( !ch.equals( "\n" ) ) {
                    y += charHeight;
                    buffer.add( ch );

                    added = true;
                }
            }

            if ( isLineFeed || isLineBreak ) {
                final float charGap = config.useJustification && !isLineFeed ? //
                        ( surface.height - config.verticalPadding - y ) / ( buffer.size() - 1 ) : 0;

                float curY = config.verticalPadding;
                for ( int bufIndex = 0 ; bufIndex < buffer.size() ; bufIndex++ ) {
                    final String curCh = buffer.get( bufIndex );

                    final int srcIndex = index - buffer.size() + bufIndex + ( added ? 1 : 0 );
                    final TCY tcy_ = getTCY( source , srcIndex );

                    if ( tcy_ != null ) {
                        final float w =
                                p.measureText( source.text.substring( tcy_.location , tcy_.location + tcy_.length ) );
                        float x_ = x - config.getRubyFontSize() - config.fontSize - ( w - config.fontSize ) / 2;

                        for ( int delta = 0 ; delta < tcy_.length ; delta++ ) {
                            ret[ srcIndex + delta ] =
                                    new TextLayoutInfo( x_ , curY , config.fontSize , config.fontSize , line , x );

                            x_ += p.measureText( source.text.substring( srcIndex + delta , srcIndex + delta + 1 ) );
                        }

                        bufIndex += tcy_.length - 1;
                    } else if ( shouldRotate( curCh ) ) {
                        ret[ srcIndex ] =
                                new TextLayoutInfo( x - config.getRubyFontSize() - config.fontSize
                                        + p.getFontMetrics().descent , curY + p.getFontMetrics().descent ,
                                        config.fontSize , measure( p , curCh ) , line , x );
                    } else {
                        final float w = p.measureText( curCh );

                        final PointF offset = getCharOffset( p , curCh );

                        ret[ srcIndex ] =
                                new TextLayoutInfo( x - config.getRubyFontSize() - config.fontSize
                                        + ( config.fontSize - w ) / 2 + offset.x , curY , config.fontSize ,
                                        config.fontSize - offset.y , line , x );
                    }

                    curY += ( tcy_ != null ? p.getTextSize() : measure( p , curCh ) ) + charGap;
                }

                x -= config.getRubyFontSize() + config.fontSize + config.lineSpace;
                y = config.verticalPadding;

                buffer.clear();
                line++;
            }

            if ( !ch.equals( "\n" ) && !added ) {
                y += charHeight;
                buffer.add( ch );

                if ( tcy != null ) {
                    for ( int delta = 1 ; delta < tcy.length ; delta++ ) {
                        index++;
                        buffer.add( source.at( index ) );
                    }
                }
            }
        }

        final float trans = config.horizontalPadding - ret[ ret.length - 1 ].x;
        for ( final TextLayoutInfo layout : ret ) {
            if ( layout != null ) {
                layout.x += trans;
                layout.lineMetrics += trans;
            }
        }

        return ret;
    }

    private float measure( final Paint p , final String ch ) {
        if ( shouldRotate( ch ) ) {
            return p.measureText( ch );
        } else {
            final boolean isHalf = "".contains( ch );

            return p.getTextSize() * ( isHalf ? 0.5f : 1f );
        }
    }

    private boolean shouldRotate( final String ch ) {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789（） ー「」".contains( ch );
    }
}
