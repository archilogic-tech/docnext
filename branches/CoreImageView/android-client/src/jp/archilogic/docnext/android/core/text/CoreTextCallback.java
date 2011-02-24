package jp.archilogic.docnext.android.core.text;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.archilogic.docnext.android.core.OnPageChangedListener;
import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.CoreTextInfo.Dot;
import jp.archilogic.docnext.android.core.text.CoreTextInfo.Ruby;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
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
    private boolean _invalidateCache = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

    private final Bitmap _background;
    private List< CoreTextInfo > _sources;
    private CoreTextConfig _config;
    private OnPageChangedListener _listener = null;
    private TextDocDirection _direction;

    private float _offset;

    private Bitmap[] _caches;
    private int _index;

    private final ExecutorService _loadingExecutor = Executors.newSingleThreadExecutor();

    public CoreTextCallback( final Bitmap background ) {
        _background = background;
    }

    private Bitmap buildCache( final Paint p , final CoreTextInfo source ) {
        final LayoutInfo[] layouts = layoutText( p , source );

        final LayoutInfo last = layouts[ layouts.length - 1 ];
        final Bitmap ret = Bitmap.createBitmap( _surfaceSize.width , //
                ( int ) Math.ceil( last.y + last.height + _config.verticalPadding ) , Config.RGB_565 );

        final Canvas c = new Canvas( ret );

        drawText( c , p , layouts , source );
        drawRubys( c , p , layouts , source );
        drawDots( c , p , layouts , source );

        return ret;
    }

    private Paint buildPaint() {
        final Paint p = new Paint();
        p.setAntiAlias( true );
        p.setTypeface( Typeface.createFromFile( "/sdcard/docnext/hira_kaku_pro_w3.otf" ) );
        return p;
    }

    public void cancelCleanUp() {
        _willCancelCleanUp = true;
    }

    private void changeToNextPage() {
        if ( _index - 1 >= 0 ) {
            _caches[ _index - 1 ] = null;
        }

        if ( _index + 2 < _sources.size() ) {
            _caches[ _index + 2 ] = null;
            load( _index + 2 );
        }

        _offset +=
                ( _caches[ _index ] != null ? _caches[ _index ].getHeight() : _surfaceSize.height ) + _config.pageSpace;

        _index++;

        if ( _listener != null ) {
            _listener.onPageChanged( _index );
        }
    }

    private void changeToPrevPage() {
        if ( _index + 1 < _caches.length ) {
            _caches[ _index + 1 ] = null;
        }

        if ( _index - 2 >= 0 ) {
            _caches[ _index - 2 ] = null;
            load( _index - 2 );
        }

        _index--;

        _offset -=
                ( _caches[ _index ] != null ? _caches[ _index ].getHeight() : _surfaceSize.height ) + _config.pageSpace;

        if ( _listener != null ) {
            _listener.onPageChanged( _index );
        }
    }

    private void checkChangePage() {
        if ( _offset < -( _caches[ _index ] != null ? _caches[ _index ].getHeight() : _surfaceSize.height )
                && _index + 1 < _sources.size() ) {
            changeToNextPage();
        } else if ( _offset > 0 && _index - 1 >= 0 ) {
            changeToPrevPage();
        }
    }

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidate = true;
        _willCancelCleanUp = false;
    }

    private void draw( final Canvas c , final Paint p ) {
        drawBackground( c , p );

        c.save();
        c.translate( 0 , _offset );

        for ( int delta = -1 ; delta <= 1 ; delta++ ) {
            if ( _index + delta >= 0 && _index + delta < _caches.length ) {
                if ( _caches[ _index + delta ] != null ) {
                    final float y =
                            delta < 1
                                    ? ( _caches[ _index + delta ].getHeight() + _config.pageSpace ) * delta
                                    : ( _caches[ _index ] != null ? _caches[ _index ].getHeight() : _surfaceSize.height )
                                            + _config.pageSpace;

                    c.drawBitmap( _caches[ _index + delta ] , null , //
                            new RectF( 0 , y , _caches[ _index + delta ].getWidth() , //
                                    y + _caches[ _index + delta ].getHeight() ) , p );
                } else {
                    final float y = ( _surfaceSize.height + _config.pageSpace ) * delta;
                    p.setColor( _config.backgroundColor );

                    c.drawRect( new RectF( 0 , y , _surfaceSize.width , y + _surfaceSize.height ) , p );
                }
            }
        }

        c.restore();
    }

    private void drawBackground( final Canvas c , final Paint p ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() , y * _background.getHeight() , p );
            }
        }
    }

    private void drawDots( final Canvas c , final Paint p , final LayoutInfo[] layouts , final CoreTextInfo source ) {
        p.setTextSize( _config.getRubyFontSize() );
        p.setColor( _config.defaultTextColor );

        final String dotChar = "﹅";
        final float w = p.measureText( dotChar );

        for ( final Dot dot : source.dots ) {
            for ( int delta = 0 ; delta < dot.length ; delta++ ) {
                final LayoutInfo l = layouts[ dot.location + delta ];

                c.drawText( dotChar , l.x + ( l.width - w ) / 2 , l.y , p );
            }
        }
    }

    private void drawRuby( final Canvas c , final Paint p , final LayoutInfo[] layouts , final Ruby ruby ) {
        final float w = p.measureText( ruby.text );

        final LayoutInfo to = layouts[ ruby.location + ruby.length - 1 ];
        final LayoutInfo from = layouts[ ruby.location ];

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

    private void drawRubys( final Canvas c , final Paint p , final LayoutInfo[] layouts , final CoreTextInfo source ) {
        p.setTextSize( _config.getRubyFontSize() );
        p.setColor( _config.defaultTextColor );

        for ( final Ruby ruby : source.rubys ) {
            final List< Integer > splitHeadIndex = Lists.newArrayList();
            float currentY = -1;
            for ( int delta = 0 ; delta < ruby.length ; delta++ ) {
                if ( layouts[ ruby.location + delta ].y != currentY ) {
                    currentY = layouts[ ruby.location + delta ].y;

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

    private void drawText( final Canvas c , final Paint p , final LayoutInfo[] layouts , final CoreTextInfo source ) {
        c.drawColor( _config.backgroundColor );

        p.setTextSize( _config.fontSize );
        p.setColor( _config.defaultTextColor );

        for ( int index = 0 ; index < layouts.length ; index++ ) {
            final LayoutInfo l = layouts[ index ];

            if ( l != null ) {
                c.drawText( source.at( index ) , l.x , l.y + l.height , p );
            }
        }
    }

    private boolean isCharNotPermittedOnStart( final String ch ) {
        return "。、".contains( ch );
    }

    private LayoutInfo[] layoutText( final Paint p , final CoreTextInfo source ) {
        final LayoutInfo[] ret = new LayoutInfo[ source.length() ];

        float x = _config.horizontalPadding;
        float y = _config.verticalPadding;

        final List< String > buffer = Lists.newArrayList();

        p.setTextSize( _config.fontSize );

        for ( int index = 0 ; index < source.length() ; index++ ) {
            final String ch = source.at( index );

            final float charWidth = measure( p , ch );
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
                    if ( index + 1 < source.length() ) {
                        final String nextCh = source.at( index + 1 );

                        if ( isCharNotPermittedOnStart( nextCh ) && //
                                x + charWidth + measure( p , nextCh ) >= _surfaceSize.width - _config.horizontalPadding ) {
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
                final float charGap = _config.useJustification && !isLineFeed ? //
                        ( _surfaceSize.width - _config.horizontalPadding - x ) / ( buffer.size() - 1 ) : 0;

                float curX = _config.horizontalPadding;
                for ( int bufIndex = 0 ; bufIndex < buffer.size() ; bufIndex++ ) {
                    final String curCh = buffer.get( bufIndex );

                    final float w = measure( p , curCh );

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

    private void load( final int index ) {
        _loadingExecutor.execute( new Runnable() {
            @Override
            public void run() {
                // 2 for waiting index update
                if ( index < _index - 2 || index > _index + 2 ) {
                    _caches[ index ] = null;
                    return;
                }

                _caches[ index ] = buildCache( buildPaint() , _sources.get( index ) );

                if ( index < _index - 1 || index > _index + 1 ) {
                    _caches[ index ] = null;
                    return;
                }

                _invalidate = true;
            }
        } );
    }

    private float measure( final Paint p , final String ch ) {
        final boolean isHalf = "。、".contains( ch );

        return p.measureText( ch ) * ( isHalf ? 0.5f : 1f );
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint p ) {
        // TODO case bottom
        final boolean needCleanUp =
                _index == 0
                        && _offset > 0
                        || _index == _caches.length - 1
                        && _offset < _surfaceSize.height
                                - ( _caches[ _index ] != null ? _caches[ _index ].getHeight() : _surfaceSize.height );

        if ( needCleanUp ) {
            final float srcOffset = _offset;
            final float dstOffset =
                    Math.max( Math.min( _offset , 0 ) , _surfaceSize.height
                            - ( _caches[ _index ] != null ? _caches[ _index ].getHeight() : _surfaceSize.height ) );

            final long t = SystemClock.elapsedRealtime();

            final Interpolator i = new AccelerateDecelerateInterpolator();

            while ( !_willCancelCleanUp ) {
                final float diff = Math.min( 1f * ( SystemClock.elapsedRealtime() - t ) / DURATION_CLEAN_UP , 1f );

                final Canvas c_ = holder.lockCanvas();

                _offset = srcOffset + ( dstOffset - srcOffset ) * i.getInterpolation( diff );

                draw( c_ , p );

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
        _invalidateCache = true;
    }

    public void setDirection( final TextDocDirection d ) {
        _direction = d;

        _invalidate = true;
        _invalidateCache = true;
    }

    public void setListener( final OnPageChangedListener l ) {
        _listener = l;
    }

    public void setSources( final List< CoreTextInfo > sources ) {
        _sources = sources;

        if ( _surfaceSize == null ) {
            return;
        }

        _index = 0;

        _caches = new Bitmap[ sources.size() ];

        _offset = 0;

        _invalidate = true;
        _invalidateCache = true;
    }

    @Override
    public void surfaceChanged( final SurfaceHolder holder , final int format , final int width , final int height ) {
        _surfaceSize = new Size( width , height );

        if ( _sources != null ) {
            setSources( _sources );
        }
    }

    @Override
    public void surfaceCreated( final SurfaceHolder holder ) {
        _shouldStop = false;

        _worker = new Thread() {
            @Override
            public void run() {
                final Paint p = buildPaint();

                while ( !_shouldStop ) {
                    if ( _invalidate && _surfaceSize != null ) {
                        _invalidate = false;

                        if ( _invalidateCache ) {
                            _invalidateCache = false;

                            for ( int delta = -1 ; delta <= 1 ; delta++ ) {
                                if ( _index + delta >= 0 && _index + delta < _caches.length ) {
                                    _caches[ _index + delta ] = null;
                                    _caches[ _index + delta ] = buildCache( p , _sources.get( _index + delta ) );
                                }
                            }
                        }

                        final Canvas c = holder.lockCanvas();

                        draw( c , p );

                        holder.unlockCanvasAndPost( c );

                        if ( _willCleanUp ) {
                            _willCleanUp = false;

                            checkChangePage();

                            runCleanUp( holder , p );
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
