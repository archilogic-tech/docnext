package jp.archilogic.docnext.android.core.text;

import java.util.List;

import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.common.collect.Lists;

public class CoreTextCallback implements SurfaceHolder.Callback {
    private static final long DURATION_CLEAN_UP = 200L;

    private Size _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidated = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

    private final Bitmap _background;
    // private List< String > _sources;
    private String _source;
    private CoreTextConfig _config;
    // private CoreImageListener _listener = null;
    private TextDocDirection _direction;

    private float _offset;

    public CoreTextCallback( final Bitmap background ) {
        _background = background;
    }

    private String at( final int index ) {
        return _source.substring( index , index + 1 );
    }

    public void cancelCleanUp() {
        _willCancelCleanUp = true;
    }

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void draw( final Canvas c , final Paint paint ) {
        drawBackground( c , paint );

        c.save();

        c.drawColor( Color.WHITE );

        drawText( c , paint );

        c.restore();
    }

    private void drawBackground( final Canvas c , final Paint paint ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() , y * _background.getHeight() , paint );
            }
        }
    }

    private void drawText( final Canvas c , final Paint paint ) {
        System.err.println( "***** drawText begin *****" );

        float x = _config.horizontalPadding;
        float y = _config.verticalPadding;

        final List< String > buffer = Lists.newArrayList();

        for ( int index = 0 ; index < _source.length() ; index++ ) {
            final String ch = at( index );

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
                        final String nextCh = at( index + 1 );
                        if ( isCharNotPermittedOnStart( nextCh ) && //
                                x + charWidth + measure( nextCh , paint ) >= _surfaceSize.width
                                        - _config.horizontalPadding ) {
                            isLineBreak = true;
                        }
                    }
                }
            }

            boolean isLineFeed = ch.equals( "\n" ) || index + 1 >= _source.length();
            if ( isLineFeed ) {
                if ( !ch.equals( "\n" ) ) {
                    x += charWidth;
                    buffer.add( ch );
                }
            }

            if ( isLineFeed || isLineBreak ) {
                final float charGap = _config.useJustification && !isLineFeed ? //
                        ( _surfaceSize.width - _config.horizontalPadding - x ) / ( buffer.size() - 1 ) : 0;

                float curX = _config.horizontalPadding;
                for ( int bufIndex = 0 ; bufIndex < buffer.size() ; bufIndex++ ) {
                    final String curCh = buffer.get( bufIndex );

                    paint.setTextSize( _config.fontSize );
                    c.drawText( curCh , curX , y + _config.getRubyFontSize() + _config.fontSize , paint );

                    curX += measure( curCh , paint ) + charGap;
                }

                x = _config.horizontalPadding;
                y += _config.getRubyFontSize() + _config.fontSize + _config.lineSpace;
                buffer.clear();
                isLineFeed = false;
            }

            if ( !ch.equals( "\n" ) && !added ) {
                x += charWidth;
                buffer.add( ch );
            }
        }

        System.err.println( "***** drawText end *****" );
    }

    private boolean isCharNotPermittedOnStart( final String ch ) {
        return "。、".contains( ch );
    }

    private float measure( final String ch , final Paint paint ) {
        final boolean isHalf = "。、".contains( ch );

        return paint.measureText( ch ) * ( isHalf ? 0.5f : 1f );
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint paint ) {
        final boolean needCleanUp = true;
        final float srcOffset = _offset;
        final float dstOffset = 0;

        if ( needCleanUp ) {
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

        _invalidated = true;
    }

    public void setDirection( final TextDocDirection d ) {
        _direction = d;

        _invalidated = true;
    }

    /*
     * public void setListener( final CoreImageListener l ) { _listener = l; }
     */

    public void setSource( final String source ) {
        _source = source;

        if ( _surfaceSize == null ) {
            return;
        }

        _offset = 0;

        _invalidated = true;
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

                while ( !_shouldStop ) {
                    if ( _invalidated && _surfaceSize != null ) {
                        _invalidated = false;

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

        _invalidated = true;
    }
}
