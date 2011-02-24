package jp.archilogic.docnext.android.core.text.engine;

import java.util.List;

import jp.archilogic.docnext.android.core.Size;
import jp.archilogic.docnext.android.core.text.CoreTextConfig;
import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.CoreTextInfo;
import jp.archilogic.docnext.android.core.text.CoreTextInfo.Dot;
import jp.archilogic.docnext.android.core.text.CoreTextInfo.Ruby;
import jp.archilogic.docnext.android.core.text.TextLayoutInfo;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.common.collect.Lists;

public class HorizontalCoreTextEngine implements CoreTextEngine {
    @Override
    public void drawDots( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final CoreTextInfo source ,
            final CoreTextConfig _config ) {
        p.setTextSize( _config.getRubyFontSize() );
        p.setColor( _config.defaultTextColor );

        final String dotChar = "﹅";
        final float w = p.measureText( dotChar );

        for ( final Dot dot : source.dots ) {
            for ( int delta = 0 ; delta < dot.length ; delta++ ) {
                final TextLayoutInfo l = layouts[ dot.location + delta ];

                c.drawText( dotChar , l.x + ( l.width - w ) / 2 , l.y , p );
            }
        }
    }

    private void drawRuby( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final Ruby ruby ) {
        final float w = p.measureText( ruby.text );

        final TextLayoutInfo to = layouts[ ruby.location + ruby.length - 1 ];
        final TextLayoutInfo from = layouts[ ruby.location ];

        final float textWidth = to.x + to.width - from.x;

        if ( textWidth > w ) {
            final float unit = ( textWidth - w ) / ruby.text.length();
            float x = from.x + unit / 2;

            for ( int index = 0 ; index < ruby.text.length() ; index++ ) {
                c.drawText( ruby.text.substring( index , index + 1 ) , x , from.y , p );

                x += p.measureText( ruby.text.substring( index , index + 1 ) ) + unit;
            }
        } else {
            c.drawText( ruby.text , from.x + ( textWidth - w ) / 2 , from.y , p );
        }
    }

    @Override
    public void drawRubys( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final CoreTextInfo source ,
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
    public void drawText( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final CoreTextInfo source ,
            final CoreTextConfig _config ) {
        c.drawColor( _config.backgroundColor );

        p.setTextSize( _config.fontSize );
        p.setColor( _config.defaultTextColor );

        for ( int index = 0 ; index < layouts.length ; index++ ) {
            final TextLayoutInfo l = layouts[ index ];

            if ( l != null ) {
                c.drawText( source.at( index ) , l.x , l.y + l.height , p );
            }
        }
    }

    private boolean isCharNotPermittedOnStart( final String ch ) {
        return "。、".contains( ch );
    }

    @Override
    public TextLayoutInfo[] layoutText( final Paint p , final CoreTextInfo source , final CoreTextConfig config ,
            final Size surface ) {
        final TextLayoutInfo[] ret = new TextLayoutInfo[ source.length() ];

        float x = config.horizontalPadding;
        float y = config.verticalPadding;

        final List< String > buffer = Lists.newArrayList();

        p.setTextSize( config.fontSize );

        int line = 0;

        for ( int index = 0 ; index < source.length() ; index++ ) {
            final String ch = source.at( index );

            final float charWidth = measure( p , ch );
            boolean added = false;

            boolean isLineBreak = x + charWidth >= surface.width - config.horizontalPadding;

            if ( isLineBreak ) {
                if ( config.lineBreakingRule == LineBreakingRule.SQUEEZE ) {
                    if ( isCharNotPermittedOnStart( ch ) ) {
                        x += charWidth;
                        buffer.add( ch );

                        added = true;
                    }
                }
            } else {
                if ( config.lineBreakingRule == LineBreakingRule.TO_NEXT ) {
                    if ( index + 1 < source.length() ) {
                        final String nextCh = source.at( index + 1 );

                        if ( isCharNotPermittedOnStart( nextCh ) && //
                                x + charWidth + measure( p , nextCh ) >= surface.width - config.horizontalPadding ) {
                            isLineBreak = true;
                        }
                    }
                }
            }

            final boolean isLineFeed = ch.equals( "\n" ) || index + 1 >= source.length();
            if ( isLineFeed ) {
                if ( !ch.equals( "\n" ) ) {
                    x += charWidth;
                    buffer.add( ch );

                    added = true;
                }
            }

            if ( isLineFeed || isLineBreak ) {
                final float charGap = config.useJustification && !isLineFeed ? //
                        ( surface.width - config.horizontalPadding - x ) / ( buffer.size() - 1 ) : 0;

                float curX = config.horizontalPadding;
                for ( int bufIndex = 0 ; bufIndex < buffer.size() ; bufIndex++ ) {
                    final String curCh = buffer.get( bufIndex );

                    final float w = measure( p , curCh );

                    ret[ index - buffer.size() + bufIndex + ( added ? 1 : 0 ) ] =
                            new TextLayoutInfo( curX , y + config.getRubyFontSize() , w , config.fontSize , line , y );

                    curX += w + charGap;
                }

                x = config.horizontalPadding;
                y += config.getRubyFontSize() + config.fontSize + config.lineSpace;

                buffer.clear();
                line++;
            }

            if ( !ch.equals( "\n" ) && !added ) {
                x += charWidth;
                buffer.add( ch );
            }
        }

        return ret;
    }

    private float measure( final Paint p , final String ch ) {
        final boolean isHalf = "。、".contains( ch );

        return p.measureText( ch ) * ( isHalf ? 0.5f : 1f );
    }
}
