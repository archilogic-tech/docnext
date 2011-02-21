package jp.archilogic.docnext.android.core_image;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class CoreImageCallback implements SurfaceHolder.Callback {
    private class TapDispatcher extends Thread {
        private final PointF _point;
        private boolean _cancelled = false;

        TapDispatcher( final PointF point ) {
            _point = point;
        }

        void cancel() {
            _cancelled = true;
        }

        @Override
        public void run() {
            try {
                Thread.sleep( DOUBLE_TAP_DURATION );
            } catch ( final InterruptedException e ) {
                throw new RuntimeException( e );
            }

            if ( !_cancelled ) {
                doTap( _point );
            }

            _tapDispatcher = null;
        }
    }

    private static final int DOUBLE_TAP_THREASHOLD = 50;
    private static final long DOUBLE_TAP_DURATION = 500;
    private static final long DURATION_CLEAN_UP = 200L;

    private Size _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidated = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;
    private boolean _willCheckChangePage = false;

    private final Bitmap _background;
    private List< String > _sources;
    private List< String > _thumbnailSources;
    private CoreImageListener _listener = null;
    private DocumentDirection _direction;

    private Size _imageSize;

    private PointF _offset;
    private float _scale; // to _sourceSize
    private float _minScale;
    private float _maxScale;

    private PointF _willOffsetTo;
    private float _willScaleTo;

    private Bitmap[] _images;

    private int _index;

    private final ExecutorService _loadingExecutor = Executors.newSingleThreadExecutor();

    private TapDispatcher _tapDispatcher = null;
    private PointF _delayPoint;

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

    private void changeToNextPage() {
        if ( _index - 1 >= 0 ) {
            _images[ _index - 1 ] = null;
        }

        if ( _index + 2 < _sources.size() ) {
            _images[ _index + 2 ] = decode( _thumbnailSources.get( _index + 2 ) );
            load( _index + 2 );
        }

        _index++;

        _direction.updateOffset( _offset , _imageSize , _scale , true );

        if ( _listener != null ) {
            _listener.onPageChanged( _index );
        }
    }

    private void changeToPrevPage() {
        if ( _index + 1 < _images.length ) {
            _images[ _index + 1 ] = null;
        }

        if ( _index - 2 >= 0 ) {
            _images[ _index - 2 ] = decode( _thumbnailSources.get( _index - 2 ) );
            load( _index - 2 );
        }

        _index--;

        _direction.updateOffset( _offset , _imageSize , _scale , false );

        if ( _listener != null ) {
            _listener.onPageChanged( _index );
        }
    }

    private void checkChangePage() {
        if ( _direction.shouldChangeToNext( _offset , _surfaceSize , _imageSize , _scale )
                && _index + 1 < _sources.size() ) {
            changeToNextPage();
        } else if ( _direction.shouldChangeToPrev( _offset , _surfaceSize , _imageSize , _scale ) && _index - 1 >= 0 ) {
            changeToPrevPage();
        }
    }

    private Bitmap decode( final String path ) {
        final Options o = new Options();
        o.inPreferredConfig = Config.RGB_565;

        return BitmapFactory.decodeFile( path , o );
    }

    public void doCleanUp( final boolean willCheckChangePage ) {
        _willCleanUp = true;
        _willCheckChangePage = willCheckChangePage;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void doDoubleTap( final PointF point ) {
        if ( _scale < _maxScale ) {
            _willOffsetTo = new PointF( //
                    _offset.x * _maxScale / _scale - //
                            ( point.x - getHorizontalPadding() ) * ( _maxScale - _scale ) / _scale , //
                    _offset.y * _maxScale / _scale - //
                            ( point.y - getVerticalPadding() ) * ( _maxScale - _scale ) / _scale );
            _willOffsetTo.x =
                    Math.min( Math.max( _willOffsetTo.x , _surfaceSize.width - _imageSize.width * _maxScale ) , 0 );
            _willOffsetTo.y =
                    Math.min( Math.max( _willOffsetTo.y , _surfaceSize.height - _imageSize.height * _maxScale ) , 0 );
            _willScaleTo = _maxScale;
        } else {
            _willOffsetTo = new PointF( 0 , 0 );
            _willScaleTo = _minScale;
        }

        _willCleanUp = true;
        _willCheckChangePage = false;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void doTap( final PointF point ) {
    }

    private void draw( final Canvas c , final Paint paint ) {
        drawBackground( c , paint );

        c.save();

        c.translate( _offset.x + getHorizontalPadding() , _offset.y + getVerticalPadding() );
        c.scale( _scale , _scale );

        for ( int delta = -1 ; delta <= 1 ; delta++ ) {
            if ( _index + delta >= 0 && _index + delta < _images.length && _images[ _index + delta ] != null ) {
                c.drawBitmap( _images[ _index + delta ] , null , //
                        new RectF( _imageSize.width * _direction.toXSign() * delta , //
                                _imageSize.height * _direction.toYSign() * delta , //
                                _imageSize.width * ( _direction.toXSign() * delta + 1 ) , //
                                _imageSize.height * ( _direction.toYSign() * delta + 1 ) ) , paint );
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

    private float getHorizontalPadding() {
        return Math.max( _surfaceSize.width - _imageSize.width * Math.max( _scale , _minScale ) , 0 ) / 2f;
    }

    private float getVerticalPadding() {
        return Math.max( _surfaceSize.height - _imageSize.height * Math.max( _scale , _minScale ) , 0 ) / 2f;
    }

    private void load( final int index ) {
        _loadingExecutor.execute( new Runnable() {
            @Override
            public void run() {
                // 2 for waiting index update
                if ( index < _index - 2 || index > _index + 2 ) {
                    _images[ index ] = null;
                    return;
                }

                _images[ index ] = decode( _sources.get( index ) );

                if ( index < _index - 1 || index > _index + 1 ) {
                    _images[ index ] = null;
                    return;
                }

                _invalidated = true;
            }
        } );
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint paint ) {
        CleanUpState state;
        if ( _willOffsetTo != null ) {
            state = CleanUpState.getInstance( _offset , _scale , _willOffsetTo , _willScaleTo );

            _willOffsetTo = null;
        } else {
            state = CleanUpState.getInstance( _offset , _scale , _surfaceSize , _imageSize , _minScale , _maxScale );
        }

        if ( state.needCleanUp ) {
            final long t = SystemClock.elapsedRealtime();

            final Interpolator i = new AccelerateDecelerateInterpolator();

            while ( !_willCancelCleanUp ) {
                final float diff = Math.min( 1f * ( SystemClock.elapsedRealtime() - t ) / DURATION_CLEAN_UP , 1f );

                final Canvas c_ = holder.lockCanvas();

                _offset.x = state.srcOffset.x + ( state.dstOffset.x - state.srcOffset.x ) * i.getInterpolation( diff );
                _offset.y = state.srcOffset.y + ( state.dstOffset.y - state.srcOffset.y ) * i.getInterpolation( diff );
                _scale = state.srcScale + ( state.dstScale - state.srcScale ) * i.getInterpolation( diff );

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

    public void scale( float delta , final PointF center ) {
        if ( _scale < _minScale || _scale > _maxScale ) {
            delta = ( float ) Math.pow( delta , 0.2 );
        }

        _scale *= delta;

        _offset.x = _offset.x * delta + ( 1 - delta ) * ( center.x - getHorizontalPadding() );
        _offset.y = _offset.y * delta + ( 1 - delta ) * ( center.y - getVerticalPadding() );

        _invalidated = true;
    }

    public void setDirection( final DocumentDirection d ) {
        _direction = d;

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

        _images = new Bitmap[ sources.size() ];
        _images[ 0 ] = decode( sources.get( 0 ) );
        _images[ 1 ] = decode( sources.get( 1 ) );

        _imageSize = new Size( _images[ 0 ].getWidth() , _images[ 0 ].getHeight() );
        _scale = _minScale = calcBaseScale( _imageSize , _surfaceSize );
        _maxScale = 1;
        _offset = new PointF( 0 , 0 );

        _invalidated = true;
    }

    public void setThumbnailSources( final List< String > thumbs ) {
        _thumbnailSources = thumbs;
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
                    if ( _invalidated && _surfaceSize != null && _imageSize != null ) {
                        _invalidated = false;

                        final Canvas c = holder.lockCanvas();

                        draw( c , paint );

                        holder.unlockCanvasAndPost( c );

                        if ( _willCleanUp ) {
                            _willCleanUp = false;

                            if ( _willCheckChangePage ) {
                                _willCheckChangePage = false;

                                checkChangePage();
                            }

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

    public void tap( final PointF point ) {
        if ( _tapDispatcher != null ) {
            _tapDispatcher.cancel();
            _tapDispatcher = null;

            if ( Math.hypot( point.x - _delayPoint.x , point.y - _delayPoint.y ) > DOUBLE_TAP_THREASHOLD ) {
                tap( point );
            } else {
                doDoubleTap( point );
            }
        } else {
            _delayPoint = point;
            _tapDispatcher = new TapDispatcher( point );
            _tapDispatcher.start();
        }
    }

    public void translate( final PointF delta ) {
        if ( _surfaceSize.width >= _imageSize.width * _scale && !_direction.canMoveHorizontal() ) {
            delta.x = 0;
        }
        if ( _surfaceSize.height >= _imageSize.height * _scale && !_direction.canMoveVertical() ) {
            delta.y = 0;
        }

        _offset.offset( delta.x , delta.y );

        _invalidated = true;
    }
}
