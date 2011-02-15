package jp.archilogic.docnext.android.widget;

import java.util.List;

import jp.archilogic.docnext.android.interpolator.EaseOutBounceInterpolator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.animation.Interpolator;

import com.google.common.collect.Lists;

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
                dstOffset.y =
                        Math.min( Math.max( dstOffset.y , Math.min( surface.height - image.height * dstScale , 0 ) ) ,
                                0 );
            } else {
                needCleanUp = false;
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

    private static final long DURATION_CLEAN_UP = 500L;

    private Size _surfaceSize;
    private Thread _worker;

    private boolean _shouldStop;
    private boolean _invalidated = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

    private List< String > _sources;
    private final Bitmap _background;

    private Size _imageSize;

    private PointF _offset;
    private float _scale; // to _sourceSize
    private float _minScale;
    private float _maxScale;

    private Bitmap _image;

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

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void doCleanUp( final SurfaceHolder holder , final Paint paint ) {
        final CleanUpState state =
                new CleanUpState( _offset , _scale , _surfaceSize , _imageSize , _minScale , _maxScale );

        if ( state.needCleanUp ) {
            final long t = SystemClock.elapsedRealtime();

            final Interpolator i = new EaseOutBounceInterpolator();

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

    private void draw( final Canvas c , final Paint paint , final PointF offset ) {
        drawBackground( c , paint );

        c.save();

        final float hPadding = Math.max( _surfaceSize.width - _imageSize.width * _scale , 0 ) / 2f;
        final float vPadding = Math.max( _surfaceSize.height - _imageSize.height * _scale , 0 ) / 2f;

        c.translate( _offset.x + hPadding , _offset.y + vPadding );
        c.scale( _scale , _scale );

        c.drawBitmap( _image , 0 , 0 , paint );

        c.restore();
    }

    private void drawBackground( final Canvas c , final Paint paint ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() , y * _background.getHeight() , paint );
            }
        }
    }

    public void scale( final float delta , final PointF center ) {
        _scale *= delta;

        _offset.x = Math.min( _offset.x * delta + center.x * ( 1 - delta ) / delta , 0 );
        _offset.y = Math.min( _offset.y * delta + center.y * ( 1 - delta ) / delta , 0 );

        _invalidated = true;
    }

    public void setSources( final List< String > sources ) {
        _sources = sources;

        _image = BitmapFactory.decodeFile( _sources.get( 0 ) );

        _imageSize = new Size( _image.getWidth() , _image.getHeight() );
        _scale = _minScale = calcBaseScale( _imageSize , _surfaceSize );
        _maxScale = 1;
        _offset = new PointF( 0 , 0 );

        _invalidated = true;
    }

    @Override
    public void surfaceChanged( final SurfaceHolder holder , final int format , final int width , final int height ) {
        _surfaceSize = new Size( width , height );

        setSources( Lists.newArrayList( "/sdcard/docnext/hanako-001.png" , "/sdcard/docnext/hanako-002.png" ,
                "/sdcard/docnext/hanako-003.png" ) );
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

                            doCleanUp( holder , paint );
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
