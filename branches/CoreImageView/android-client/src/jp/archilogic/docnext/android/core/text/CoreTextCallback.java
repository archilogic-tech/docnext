package jp.archilogic.docnext.android.core.text;

import java.util.List;

import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.CoreTextInfo.Ruby;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.common.collect.Lists;

public class CoreTextCallback implements SurfaceHolder.Callback {
    private static class LayoutInfo {
        float x;
        float y;
        float width;
        float height;

        LayoutInfo( final float x , final float y , final float width , final float height ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static final long DURATION_CLEAN_UP = 200L;

    private Size _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidate = false;
    private boolean _invalidatedCache = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

    private final Bitmap _background;
    private CoreTextInfo _source;
    private CoreTextConfig _config;
    // private CoreImageListener _listener = null;
    private TextDocDirection _direction;

    private float _offset;

    private Bitmap _cache;

    public CoreTextCallback( final Bitmap background ) {
        _background = background;
    }

    public void cancelCleanUp() {
        _willCancelCleanUp = true;
    }

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidate = true;
        _willCancelCleanUp = false;
    }

    private void draw( final Canvas c , final Paint paint ) {
        drawBackground( c , paint );

        c.save();
        c.translate( 0 , _offset );

        if ( _cache == null || _invalidatedCache ) {
            _invalidatedCache = false;

            final long t = SystemClock.elapsedRealtime();
            final LayoutInfo[] layouts = layoutText( paint );
            System.err.println( "layout: " + ( SystemClock.elapsedRealtime() - t ) );

            final LayoutInfo last = layouts[ layouts.length - 1 ];
            _cache =
                    Bitmap.createBitmap( _surfaceSize.width ,
                            ( int ) Math.ceil( last.y + last.height + _config.verticalPadding ) , Config.ARGB_8888 );

            final Canvas cacheCanvas = new Canvas( _cache );

            drawText( cacheCanvas , paint , layouts );
            drawRubys( cacheCanvas , paint , layouts );
        }

        c.drawBitmap( _cache , 0 , 0 , paint );

        c.restore();
    }

    private void drawBackground( final Canvas c , final Paint paint ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() , y * _background.getHeight() , paint );
            }
        }
    }

    private void drawRubys( final Canvas c , final Paint paint , final LayoutInfo[] layouts ) {
        paint.setTextSize( _config.getRubyFontSize() );
        paint.setColor( _config.defaultTextColor );

        for ( final Ruby ruby : _source.rubys ) {
            // TODO consider line break

            final float w = paint.measureText( ruby.text );

            final LayoutInfo to = layouts[ ruby.location + ruby.length - 1 ];
            final LayoutInfo from = layouts[ ruby.location ];

            final float textWidth = to.x + to.width - from.x;

            c.drawText( ruby.text , from.x + ( textWidth - w ) / 2 , from.y , paint );
        }
    }

    private void drawText( final Canvas c , final Paint paint , final LayoutInfo[] layouts ) {
        c.drawColor( _config.backgroundColor );

        paint.setTextSize( _config.fontSize );
        paint.setColor( _config.defaultTextColor );

        for ( int index = 0 ; index < layouts.length ; index++ ) {
            final LayoutInfo l = layouts[ index ];

            if ( l != null ) {
                c.drawText( _source.at( index ) , l.x , l.y + l.height , paint );
            }
        }
    }

    private boolean isCharNotPermittedOnStart( final String ch ) {
        return "。、".contains( ch );
    }

    private LayoutInfo[] layoutText( final Paint paint ) {
        final LayoutInfo[] ret = new LayoutInfo[ _source.length() ];

        float x = _config.horizontalPadding;
        float y = _config.verticalPadding;

        final List< String > buffer = Lists.newArrayList();

        paint.setTextSize( _config.fontSize );

        for ( int index = 0 ; index < _source.length() ; index++ ) {
            final String ch = _source.at( index );

            final float charWidth = measure( ch , paint );
            boolean added = false;

            boolean isLineBreak = x + charWidth >= _surfaceSize.width - _config.horizontalPadding;

            if ( isLineBreak ) {
                if ( _config.lineBreakingRule == LineBreakingRule.SQUEEZE ) {
                    if ( isCharNotPermittedOnStart( ch ) ) {
                        x += charWidth;
                        buffer.add( ch );

                        added = true;
                    }
                }
            } else {
                if ( _config.lineBreakingRule == LineBreakingRule.TO_NEXT ) {
                    if ( index + 1 < _source.length() ) {
                        final String nextCh = _source.at( index + 1 );

                        if ( isCharNotPermittedOnStart( nextCh ) && //
                                x + charWidth + measure( nextCh , paint ) >= _surfaceSize.width
                                        - _config.horizontalPadding ) {
                            isLineBreak = true;
                        }
                    }
                }
            }

            final boolean isLineFeed = ch.equals( "\n" ) || index + 1 >= _source.length();
            if ( isLineFeed ) {
                if ( !ch.equals( "\n" ) ) {
                    x += charWidth;
                    buffer.add( ch );

                    added = true;
                }
            }

            if ( isLineFeed || isLineBreak ) {
                final float charGap = _config.useJustification && !isLineFeed ? //
                        ( _surfaceSize.width - _config.horizontalPadding - x ) / ( buffer.size() - 1 ) : 0;

                float curX = _config.horizontalPadding;
                for ( int bufIndex = 0 ; bufIndex < buffer.size() ; bufIndex++ ) {
                    final String curCh = buffer.get( bufIndex );

                    final float w = measure( curCh , paint );

                    ret[ index - buffer.size() + bufIndex + ( added ? 1 : 0 ) ] =
                            new LayoutInfo( curX , y + _config.getRubyFontSize() , w , _config.fontSize );

                    curX += w + charGap;
                }

                x = _config.horizontalPadding;
                y += _config.getRubyFontSize() + _config.fontSize + _config.lineSpace;

                buffer.clear();
            }

            if ( !ch.equals( "\n" ) && !added ) {
                x += charWidth;
                buffer.add( ch );
            }
        }

        return ret;
    }

    private float measure( final String ch , final Paint paint ) {
        final boolean isHalf = "。、".contains( ch );

        return paint.measureText( ch ) * ( isHalf ? 0.5f : 1f );
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint paint ) {
        final boolean needCleanUp = _offset > 0 || _offset < _surfaceSize.height - _cache.getHeight();

        if ( needCleanUp ) {
            final float srcOffset = _offset;
            final float dstOffset = Math.max( Math.min( _offset , 0 ) , _surfaceSize.height - _cache.getHeight() );

            final long t = SystemClock.elapsedRealtime();

            final Interpolator i = new AccelerateDecelerateInterpolator();

            while ( !_willCancelCleanUp ) {
                final float diff = Math.min( 1f * ( SystemClock.elapsedRealtime() - t ) / DURATION_CLEAN_UP , 1f );

                final Canvas c_ = holder.lockCanvas();

                _offset = srcOffset + ( dstOffset - srcOffset ) * i.getInterpolation( diff );

                draw( c_ , paint );

                holder.unlockCanvasAndPost( c_ );

                if ( diff == 1f ) {
                    break;
                }

                try {
                    Thread.sleep( 10 );
                } catch ( final InterruptedException e ) {
                    throw new RuntimeException( e );
                }
            }
        }
    }

    public void setConfig( final CoreTextConfig c ) {
        _config = c;

        _invalidate = true;
        _invalidatedCache = true;
    }

    public void setDirection( final TextDocDirection d ) {
        _direction = d;

        _invalidate = true;
        _invalidatedCache = true;
    }

    /*
     * public void setListener( final CoreImageListener l ) { _listener = l; }
     */

    public void setSource( final CoreTextInfo source ) {
        _source = source;

        if ( _surfaceSize == null ) {
            return;
        }

        _offset = 0;

        _invalidate = true;
        _invalidatedCache = true;
    }

    @Override
    public void surfaceChanged( final SurfaceHolder holder , final int format , final int width , final int height ) {
        _surfaceSize = new Size( width , height );

        if ( _source != null ) {
            setSource( _source );
        }
    }

    @Override
    public void surfaceCreated( final SurfaceHolder holder ) {
        _shouldStop = false;

        _worker = new Thread() {
            @Override
            public void run() {
                final Paint paint = new Paint();
                paint.setAntiAlias( true );
                paint.setTypeface( Typeface.createFromFile( "/sdcard/docnext/hira_kaku_pro_w3.otf" ) );

                while ( !_shouldStop ) {
                    if ( _invalidate && _surfaceSize != null ) {
                        _invalidate = false;

                        final Canvas c = holder.lockCanvas();

                        draw( c , paint );

                        holder.unlockCanvasAndPost( c );

                        if ( _willCleanUp ) {
                            _willCleanUp = false;

                            runCleanUp( holder , paint );
                        }
                    }

                    try {
                        Thread.sleep( 10 );
                    } catch ( final InterruptedException e ) {
                        throw new RuntimeException( e );
                    }
                }
            }
        };

        _worker.start();
    }

    @Override
    public void surfaceDestroyed( final SurfaceHolder holder ) {
        _shouldStop = true;
        _worker = null;
    }

    public void translate( final PointF delta ) {
        _offset += delta.y;

        _invalidate = true;
    }
}
