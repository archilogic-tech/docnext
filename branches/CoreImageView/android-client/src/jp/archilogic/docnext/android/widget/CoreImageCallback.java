package jp.archilogic.docnext.android.widget;

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

    public enum DocumentDirection {
        L2R , R2L , T2B , B2T;

        boolean canMoveHorizontal() {
            switch ( this ) {
            case L2R:
            case R2L:
                return true;
            case T2B:
            case B2T:
                return false;
            default:
                throw new RuntimeException();
            }
        }

        boolean canMoveVertical() {
            return !canMoveHorizontal();
        }

        boolean shouldChangeToNext( final PointF offset , final Size surface , final Size image , final float scale ) {
            switch ( this ) {
            case L2R:
                return offset.x < surface.width - surface.width / PAGE_CHANGE_THREASHOLD - image.width * scale;
            case R2L:
                return offset.x > surface.width / PAGE_CHANGE_THREASHOLD;
            case T2B:
                return offset.y < surface.height - surface.height / PAGE_CHANGE_THREASHOLD - image.height * scale;
            case B2T:
                return offset.y > surface.height / PAGE_CHANGE_THREASHOLD;
            default:
                throw new RuntimeException();
            }
        }

        boolean shouldChangeToPrev( final PointF offset , final Size surface , final Size image , final float scale ) {
            switch ( this ) {
            case L2R:
                return offset.x > surface.width / PAGE_CHANGE_THREASHOLD;
            case R2L:
                return offset.x < surface.width - surface.width / PAGE_CHANGE_THREASHOLD - image.width * scale;
            case T2B:
                return offset.y > surface.height / PAGE_CHANGE_THREASHOLD;
            case B2T:
                return offset.y < surface.height - surface.height / PAGE_CHANGE_THREASHOLD - image.height * scale;
            default:
                throw new RuntimeException();
            }
        }

        int toXSign() {
            switch ( this ) {
            case L2R:
                return 1;
            case R2L:
                return -1;
            case T2B:
            case B2T:
                return 0;
            default:
                throw new RuntimeException();
            }
        }

        int toYSign() {
            switch ( this ) {
            case L2R:
            case R2L:
                return 0;
            case T2B:
                return 1;
            case B2T:
                return -1;
            default:
                throw new RuntimeException();
            }
        }

        void updateOffset( final PointF offset , final Size image , final float scale , final boolean isNext ) {
            final int sign = ( this == L2R || this == T2B ) ^ isNext ? -1 : 1;

            switch ( this ) {
            case L2R:
            case R2L:
                offset.x += sign * image.width * scale;
                break;
            case T2B:
            case B2T:
                offset.y += sign * image.height * scale;
                break;
            default:
                throw new RuntimeException();
            }
        }
    }

    private static class Size {
        int width;
        int height;

        Size( final int width , final int height ) {
            this.width = width;
            this.height = height;
        }
    }

    private static final int DOUBLE_TAP_THREASHOLD = 10;
    private static final int PAGE_CHANGE_THREASHOLD = 4;
    private static final long DURATION_CLEAN_UP = 200L;

    private Size _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidated = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

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

    private Bitmap[] _images;

    private int _index;

    private final ExecutorService _loadingExecutor = Executors.newSingleThreadExecutor();

    private Thread _tapDelayDispatcher = null;
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

        if ( _listener != null ) {
            _listener.onPageChanged( _index );
        }
    }

    private void checkChangePage() {
        if ( _direction.shouldChangeToNext( _offset , _surfaceSize , _imageSize , _scale )
                && _index + 1 < _sources.size() ) {
            changeToNextPage();

            _direction.updateOffset( _offset , _imageSize , _scale , true );
        } else if ( _direction.shouldChangeToPrev( _offset , _surfaceSize , _imageSize , _scale ) && _index - 1 >= 0 ) {
            changeToPrevPage();

            _direction.updateOffset( _offset , _imageSize , _scale , false );
        }
    }

    private Bitmap decode( final String path ) {
        final Options o = new Options();
        o.inPreferredConfig = Config.RGB_565;

        return BitmapFactory.decodeFile( path , o );
    }

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void doDoubleTap( final PointF point ) {
        System.err.println( "***** double tap *****" );
    }

    private void doTap( final PointF point ) {
        System.err.println( "***** tap *****" );
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

    private void load( final int index ) {
        _loadingExecutor.execute( new Runnable() {
            @Override
            public void run() {
                _images[ index ] = decode( _sources.get( index ) );

                if ( index < _index - 1 || index > _index + 1 ) {
                    _images[ index ] = null;
                }

                _invalidated = true;
            }
        } );
    }

    private void runCleanUp( final SurfaceHolder holder , final Paint paint ) {
        checkChangePage();

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

    public void tap( final PointF point ) {
        if ( _tapDelayDispatcher != null ) {
            _tapDelayDispatcher = null;

            if ( Math.hypot( point.x - _delayPoint.x , point.y - _delayPoint.y ) > DOUBLE_TAP_THREASHOLD ) {
                tap( point );
            }

            doDoubleTap( point );
        } else {
            _delayPoint = point;
            _tapDelayDispatcher = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep( 500 );
                    } catch ( final InterruptedException e ) {
                        throw new RuntimeException( e );
                    }

                    if ( _tapDelayDispatcher != null ) {
                        _tapDelayDispatcher = null;

                        doTap( point );
                    }
                }
            };

            _tapDelayDispatcher.start();
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
