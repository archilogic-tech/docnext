package jp.archilogic.docnext.android.coreview.text;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.coreview.text.engine.CoreTextEngine;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.SizeInfo;
import jp.archilogic.docnext.android.info.TextInfo;
import jp.archilogic.docnext.android.provider.local.LocalPathManager;
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
    private static final long DURATION_CLEAN_UP = 200L;

    private SizeInfo _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidate = false;
    private boolean _invalidateCache = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;
    private boolean _initialized = false;

    private final Bitmap _background;
    private long _id = -1;
    private List< TextInfo > _sources;
    private CoreTextConfig _config;

    private float _offset;

    private Bitmap[] _caches;
    private int _index;

    private final ExecutorService _loadingExecutor = Executors.newSingleThreadExecutor();

    public CoreTextCallback( final Bitmap background ) {
        _background = background;
    }

    void beginInteraction() {
        _willCancelCleanUp = true;
    }

    private Bitmap buildCache( final Paint p , final TextInfo source ) {
        final CoreTextEngine engine = _config.direction.getEngine();

        final TextLayoutInfo[] layouts = engine.layoutText( p , source , _config , _surfaceSize );

        final SizeInfo size = _config.direction.toCacheSize( layouts , _surfaceSize , _config );
        final Bitmap ret = Bitmap.createBitmap( size.width , size.height , Config.RGB_565 );

        final Canvas c = new Canvas( ret );

        engine.drawText( c , p , layouts , source , _config );
        engine.drawRubys( c , p , layouts , source , _config );
        engine.drawDots( c , p , layouts , source , _config );

        return ret;
    }

    private Paint buildPaint() {
        final Paint p = new Paint();
        p.setAntiAlias( true );
        p.setTypeface( Typeface.createFromFile( new LocalPathManager().getFontPath( "default" ) ) );

        return p;
    }

    private void changeToNextPage() {
        if ( _index - 1 >= 0 ) {
            _caches[ _index - 1 ] = null;
        }

        if ( _index + 2 < _sources.size() ) {
            _caches[ _index + 2 ] = null;
            load( _index + 2 );
        }

        _offset =
                _config.direction.updateOffset( _offset , nullSafeCacheSize( _index ) ,
                        _surfaceSize , _config , true );

        _index++;
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

        _offset =
                _config.direction.updateOffset( _offset , nullSafeCacheSize( _index ) ,
                        _surfaceSize , _config , false );
    }

    private void checkChangePage() {
        if ( _config.direction.shouldChangeToNext( _offset , nullSafeCacheSize( _index ) ,
                _surfaceSize ) && _index + 1 < _sources.size() ) {
            changeToNextPage();
        } else if ( _config.direction.shouldChangeToPrev( _offset , nullSafeCacheSize( _index ) ,
                _surfaceSize ) && _index - 1 >= 0 ) {
            changeToPrevPage();
        }
    }

    void drag( final PointF delta ) {
        _offset = _config.direction.translate( _offset , delta );

        _invalidate = true;
    }

    private void draw( final Canvas c , final Paint p ) {
        drawBackground( c , p );

        c.save();
        c.translate( _offset * _config.direction.toXFactor() ,
                _offset * _config.direction.toYFactor() );

        for ( int delta = -1 ; delta <= 1 ; delta++ ) {
            if ( _index + delta >= 0 && _index + delta < _caches.length ) {
                final RectF rect =
                        _config.direction.toDrawRect( _caches , _surfaceSize , _index , delta ,
                                _config.pageSpace );

                if ( _caches[ _index + delta ] != null ) {
                    c.drawBitmap( _caches[ _index + delta ] , null , rect , p );
                } else {
                    p.setColor( _config.backgroundColor );
                    c.drawRect( rect , p );
                }
            }
        }

        c.restore();
    }

    private void drawBackground( final Canvas c , final Paint p ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() ,
                        y * _background.getHeight() , p );
            }
        }
    }

    void endInteraction() {
        _willCleanUp = true;
        _invalidate = true;
        _willCancelCleanUp = false;
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

    private SizeInfo nullSafeCacheSize( final int index ) {
        return _caches[ index ] != null ? new SizeInfo( _caches[ index ].getWidth() ,
                _caches[ index ].getHeight() ) : null;
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint p ) {
        final TextCleanUpState state =
                TextCleanUpState.getInstance( _config.direction , _offset , _surfaceSize ,
                        nullSafeCacheSize( _index ) , _index , _caches.length );

        if ( state.needCleanUp ) {
            final long t = SystemClock.elapsedRealtime();

            final Interpolator i = new AccelerateDecelerateInterpolator();

            while ( !_willCancelCleanUp ) {
                final float diff =
                        Math.min( 1f * ( SystemClock.elapsedRealtime() - t ) / DURATION_CLEAN_UP ,
                                1f );

                final Canvas c_ = holder.lockCanvas();

                _offset =
                        state.srcOffset + ( state.dstOffset - state.srcOffset )
                                * i.getInterpolation( diff );

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

    void setConfig( final CoreTextConfig c ) {
        _config = c;

        _invalidate = true;
        _invalidateCache = true;
    }

    void setId( final long id ) {
        _id = id;

        if ( _surfaceSize == null ) {
            return;
        }

        final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

        _sources = Lists.newArrayList();
        for ( int page = 0 ; page < doc.pages ; page++ ) {
            _sources.add( Kernel.getLocalProvider().getText( id , page ) );
        }

        // _index = 0;

        _caches = new Bitmap[ doc.pages ];

        _offset = 0;

        _initialized = true;
        _invalidate = true;
        _invalidateCache = true;
    }

    void setPage( final int page ) {
        _index = page;
    }

    @Override
    public void surfaceChanged( final SurfaceHolder holder , final int format , final int width ,
            final int height ) {
        _surfaceSize = new SizeInfo( width , height );

        if ( !_initialized ) {
            setId( _id );
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
                    if ( _invalidate && _initialized ) {
                        _invalidate = false;

                        if ( _invalidateCache ) {
                            _invalidateCache = false;

                            for ( int delta = -1 ; delta <= 1 ; delta++ ) {
                                if ( _index + delta >= 0 && _index + delta < _caches.length ) {
                                    _caches[ _index + delta ] = null;
                                    _caches[ _index + delta ] =
                                            buildCache( p , _sources.get( _index + delta ) );
                                }
                            }

                            switch ( _config.direction ) {
                            case HORIZONTAL:
                                _offset = 0;
                                break;
                            case VERTICAL:
                                _offset = _surfaceSize.width - _caches[ _index ].getWidth();
                                break;
                            default:
                                throw new RuntimeException();
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
}
