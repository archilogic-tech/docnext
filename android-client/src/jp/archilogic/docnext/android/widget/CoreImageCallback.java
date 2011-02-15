package jp.archilogic.docnext.android.widget;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * TODO separate image loading to Delegate?
 */
public class CoreImageCallback implements SurfaceHolder.Callback {
    private static class CleanUpState {
        boolean needCleanUp;

        PointF srcOffset;
        float srcScale;
        PointF dstOffset;
        float dstScale;

        CleanUpState( final PointF offset , final float scale , final Size surface , final Size image ,
                final float minScale , final float maxScale ) {
            if ( offset.x < Math.min( surface.width - image.width * scale , 0 ) || //
                    offset.y < Math.min( surface.height - image.height * scale , 0 ) || //
                    offset.x > 0 || offset.y > 0 || scale < minScale || scale > maxScale ) {
                needCleanUp = true;

                srcOffset = new PointF( offset.x , offset.y );
                srcScale = scale;

                if ( scale < minScale ) {
                    dstScale = minScale;
                    dstOffset = new PointF( 0 , 0 );
                } else if ( scale > maxScale ) {
                    dstScale = maxScale;
                    dstOffset =
                            new PointF( ( maxScale * offset.x - ( maxScale - scale ) * surface.width / 2 ) / scale ,
                                    ( maxScale * offset.y - ( maxScale - scale ) * surface.height / 2 ) / scale );
                } else {
                    dstScale = scale;
                    dstOffset = new PointF( offset.x , offset.y );
                }

                dstOffset.x =
                        Math.min( Math.max( dstOffset.x , Math.min( surface.width - image.width * dstScale , 0 ) ) , 0 );
                dstOffset.y = Math.min( Math.max( dstOffset.y , //
                        Math.min( surface.height - image.height * dstScale , 0 ) ) , 0 );
            } else {
                needCleanUp = false;
            }
        }
    }

    public interface CoreImageListener {
        void onPageChanged( int index );
    }

    private static class Size {
        int width;
        int height;

        Size( final int width , final int height ) {
            this.width = width;
            this.height = height;
        }
    }

    private static final int PAGE_CHANGE_THREASHOLD = 4;
    private static final long DURATION_CLEAN_UP = 200L;
    private static final int PREV = 0;
    private static final int CURRENT = 1;
    private static final int NEXT = 2;

    private Size _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidated = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

    private final Bitmap _background;
    private List< String > _sources;
    private CoreImageListener _listener = null;

    private Size _imageSize;

    private PointF _offset;
    private float _scale; // to _sourceSize
    private float _minScale;
    private float _maxScale;

    private final Bitmap[] _images = new Bitmap[ 3 ];

    private int _index;
    private boolean _loading;

    public CoreImageCallback( final Bitmap background ) {
        _background = background;
    }

    private float calcBaseScale( final Size srcSize , final Size dstSize ) {
        if ( srcSize.width > dstSize.width || srcSize.height > dstSize.height ) {
            return Math.min( 1f * dstSize.width / srcSize.width , 1f * dstSize.height / srcSize.height );
        } else {
            return 1f;
        }
    }

    public void cancelCleanUp() {
        _willCancelCleanUp = true;
    }

    private void checkChangePage() {
        if ( _offset.x > _surfaceSize.width / PAGE_CHANGE_THREASHOLD && _index + 1 < _sources.size() ) {
            _images[ PREV ] = _images[ CURRENT ];
            _images[ CURRENT ] = _images[ NEXT ];
            _images[ NEXT ] = null;

            if ( _index + 2 < _sources.size() ) {
                load( _index + 2 , NEXT );
            }

            _offset.x -= _imageSize.width * _scale;
            _index++;

            if ( _listener != null ) {
                _listener.onPageChanged( _index );
            }
        } else if ( _offset.x < _surfaceSize.width - _surfaceSize.width / PAGE_CHANGE_THREASHOLD - //
                _imageSize.width * _scale && _index - 1 >= 0 ) {
            _images[ NEXT ] = _images[ CURRENT ];
            _images[ CURRENT ] = _images[ PREV ];
            _images[ PREV ] = null;

            if ( _index - 2 >= 0 ) {
                load( _index - 2 , PREV );
            }

            _offset.x += _imageSize.width * _scale;
            _index--;

            if ( _listener != null ) {
                _listener.onPageChanged( _index );
            }
        }
    }

    private Bitmap decode( final int index ) {
        final Options o = new Options();
        o.inPreferredConfig = Config.RGB_565;

        return BitmapFactory.decodeFile( _sources.get( index ) , o );
    }

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void draw( final Canvas c , final Paint paint , final PointF offset ) {
        drawBackground( c , paint );

        c.save();

        final float hPadding =
                Math.max( _surfaceSize.width - _imageSize.width * Math.max( _scale , _minScale ) , 0 ) / 2f;
        final float vPadding =
                Math.max( _surfaceSize.height - _imageSize.height * Math.max( _scale , _minScale ) , 0 ) / 2f;

        c.translate( _offset.x + hPadding , _offset.y + vPadding );
        c.scale( _scale , _scale );

        // TODO consider l2r, r2l, horizontal, vertical
        for ( int index = 0 ; index < 3 ; index++ ) {
            if ( _images[ index ] != null ) {
                c.drawBitmap( _images[ index ] , _imageSize.width * ( 1 - index ) , 0 , paint );
            }
        }

        c.restore();
    }

    private void drawBackground( final Canvas c , final Paint paint ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() , y * _background.getHeight() , paint );
            }
        }
    }

    private void load( final int sourceIndex , final int cacheIndex ) {
        _loading = true;

        new Thread() {
            @Override
            public void run() {
                _images[ cacheIndex ] = decode( sourceIndex );

                _loading = false;
            }
        }.start();
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint paint ) {
        if ( !_loading ) {
            checkChangePage();
        }

        final CleanUpState state =
                new CleanUpState( _offset , _scale , _surfaceSize , _imageSize , _minScale , _maxScale );

        if ( state.needCleanUp ) {
            final long t = SystemClock.elapsedRealtime();

            final Interpolator i = new AccelerateDecelerateInterpolator();

            while ( !_willCancelCleanUp ) {
                final float diff = Math.min( 1f * ( SystemClock.elapsedRealtime() - t ) / DURATION_CLEAN_UP , 1f );

                final Canvas c_ = holder.lockCanvas();

                _offset.x = state.srcOffset.x + ( state.dstOffset.x - state.srcOffset.x ) * i.getInterpolation( diff );
                _offset.y = state.srcOffset.y + ( state.dstOffset.y - state.srcOffset.y ) * i.getInterpolation( diff );
                _scale = state.srcScale + ( state.dstScale - state.srcScale ) * i.getInterpolation( diff );

                draw( c_ , paint , _offset );

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

    public void scale( final float delta , final PointF center ) {
        _scale *= delta;

        _offset.x = _offset.x * delta + center.x * ( 1 - delta ) / delta;
        _offset.y = _offset.y * delta + center.y * ( 1 - delta ) / delta;

        _invalidated = true;
    }

    public void setListener( final CoreImageListener l ) {
        _listener = l;
    }

    public void setSources( final List< String > sources ) {
        _sources = sources;

        if ( _surfaceSize == null ) {
            return;
        }

        _index = 0;
        _images[ CURRENT ] = decode( 0 );
        _images[ NEXT ] = decode( 1 );

        _imageSize = new Size( _images[ CURRENT ].getWidth() , _images[ CURRENT ].getHeight() );
        _scale = _minScale = calcBaseScale( _imageSize , _surfaceSize );
        _maxScale = 1;
        _offset = new PointF( 0 , 0 );

        _loading = false;
        _invalidated = true;
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
                final Paint paint = new Paint();
                paint.setAntiAlias( true );
                paint.setFilterBitmap( true );

                while ( !_shouldStop ) {
                    if ( _invalidated ) {
                        _invalidated = false;

                        final Canvas c = holder.lockCanvas();

                        draw( c , paint , _offset );

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
        _offset.offset( delta.x , delta.y );

        _invalidated = true;
    }
}
