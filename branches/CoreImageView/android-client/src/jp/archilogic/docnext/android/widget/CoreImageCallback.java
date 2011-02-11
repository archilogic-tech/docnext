package jp.archilogic.docnext.android.widget;

import java.util.List;

import jp.archilogic.docnext.android.interpolator.EaseOutBounceInterpolator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.animation.Interpolator;

import com.google.common.collect.Lists;

public class CoreImageCallback implements SurfaceHolder.Callback {
    private class Size {
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

    private boolean _invalidated = false;
    private boolean _shouldStop = false;
    private boolean _willCleanUp = false;
    private boolean _willCancelCleanUp = false;

    private List< String > _sources;
    private final Bitmap _background;

    private Size _sourceSize;

    private PointF _offset;
    private float _scale; // to _sourceSize
    private float _minScale;
    private float _maxScale;

    private Bitmap _baseCache;
    private int _baseSampleSize;

    // private Bitmap _currentCache;

    public CoreImageCallback( final Bitmap background ) {
        _background = background;
    }

    private float calcBaseScale( final String path , final Size srcSize , final Size dstSize ) {
        if ( srcSize.width > dstSize.width || srcSize.height > dstSize.height ) {
            return Math.min( 1f * dstSize.width / srcSize.width , 1f * dstSize.height / srcSize.height );
        } else {
            return 1f;
        }
    }

    public void cancelCleanUp() {
        _willCancelCleanUp = true;
    }

    private Bitmap decodeByScale( final String path , final int sampleSize ) {
        final BitmapFactory.Options sample = new BitmapFactory.Options();
        sample.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile( path , sample );
    }

    public void doCleanUp() {
        _willCleanUp = true;
        _invalidated = true;
        _willCancelCleanUp = false;
    }

    private void draw( final Canvas c , final Paint paint , final PointF offset ) {
        drawBackground( c , paint );

        final boolean f = false;
        if ( f ) {
            final RectF dst = new RectF();

            final float hPadding = Math.max( _surfaceSize.width - _sourceSize.width * _scale , 0 ) / 2f;
            dst.left = Math.max( hPadding + offset.x , 0 );
            dst.right = _surfaceSize.width - Math.max( hPadding - offset.x , 0 );

            final float vPadding = Math.max( _surfaceSize.height - _sourceSize.height * _scale , 0 ) / 2f;
            dst.top = Math.max( vPadding + offset.y , 0 );
            dst.bottom = _surfaceSize.height - Math.max( vPadding - offset.y , 0 );

            final Rect src =
                    new Rect( Math.round( surfaceToImageValue( Math.max( 0 , -( offset.x + hPadding ) ) ) ) , //
                            Math.round( surfaceToImageValue( Math.max( 0 , -( offset.y + vPadding ) ) ) ) , //
                            Math.round( surfaceToImageValue( _surfaceSize.width - hPadding
                                    - Math.max( hPadding , offset.x ) )
                                    / _scale * _minScale ) , //
                            Math.round( surfaceToImageValue( _surfaceSize.height - vPadding
                                    - Math.max( vPadding , offset.y ) )
                                    / _scale * _minScale ) );
            Math.round( ( _baseCache.getHeight() - surfaceToImageValue( Math.max( 0 , offset.y - vPadding ) ) )
                    / _scale * _minScale );

            c.drawBitmap( _baseCache , src , dst , paint );
        } else {
            c.save();

            final float hPadding = Math.max( _surfaceSize.width - _baseCache.getWidth() * _scale , 0 ) / 2f;
            final float vPadding = Math.max( _surfaceSize.height - _baseCache.getHeight() * _scale , 0 ) / 2f;

            c.translate( _offset.x + hPadding , _offset.y + vPadding );
            c.scale( _scale , _scale );

            c.drawBitmap( _baseCache , 0 , 0 , paint );

            c.restore();
        }
    }

    private void drawBackground( final Canvas c , final Paint paint ) {
        for ( int y = 0 ; y * _background.getHeight() < _surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.getWidth() < _surfaceSize.width ; x++ ) {
                c.drawBitmap( _background , x * _background.getWidth() , y * _background.getHeight() , paint );
            }
        }
    }

    private Size getSourceSize( final String path ) {
        final BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( path , bounds );

        return new Size( bounds.outWidth , bounds.outHeight );
    }

    private float imageToSurfaceValue( final float value ) {
        return value * _surfaceSize.width / _sourceSize.width * _baseSampleSize;
    }

    public void pinch( final PointF prev0 , final PointF prev1 , final PointF cur0 , final PointF cur1 ) {
        final boolean f = false;
        if ( f ) {
            final float prevDx = prev0.x - prev1.x;
            final float scaleX = prevDx != 0 ? Math.abs( ( cur0.x - cur1.x ) / prevDx ) : Float.MAX_VALUE;
            final float prevDy = prev0.y - prev1.y;
            final float scaleY = prevDy != 0 ? Math.abs( ( cur0.y - cur1.y ) / prevDy ) : Float.MAX_VALUE;

            final float scale = scaleX * _sourceSize.height < scaleY * _sourceSize.width ? scaleX : scaleY;

            final PointF center =
                    new PointF( ( scale * ( prev0.x + prev1.x ) / 2f - ( cur0.x + cur0.x ) / 2f ) / ( scale - 1 ) , //
                            ( scale * ( prev0.y + prev1.y ) / 2f - ( cur0.y + cur0.y ) / 2f ) / ( scale - 1 ) );

            scale( scale , center );
        } else {
            final float prevD = ( float ) Math.hypot( prev0.x - prev1.x , prev0.y - prev1.y );
            final float curD = ( float ) Math.hypot( cur0.x - cur1.x , cur0.y - cur1.y );

            final PointF center = new PointF( ( cur0.x + cur1.x ) / 2 , ( cur0.y + cur1.y ) / 2 );

            scale( curD / prevD , center );
        }
    }

    public void scale( final float delta , final PointF center ) {
        _scale *= delta;

        _offset.x = Math.min( _offset.x * delta + center.x * ( 1 - delta ) / delta , 0 );
        _offset.y = Math.min( _offset.y * delta + center.y * ( 1 - delta ) / delta , 0 );

        // System.err.println( _offset.x + ", " + _offset.y );
        // System.err.println( center.x + ", " + center.y );

        _invalidated = true;
    }

    public void setSources( final List< String > sources ) {
        _sources = sources;

        _sourceSize = getSourceSize( _sources.get( 0 ) );
        _scale = _minScale = calcBaseScale( _sources.get( 0 ) , _sourceSize , _surfaceSize );
        _maxScale = 1;
        _baseSampleSize = ( int ) Math.floor( 1f / _scale );
        _baseSampleSize = 1;
        _baseCache = decodeByScale( sources.get( 0 ) , _baseSampleSize );
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

                            final float minOffsetX = Math.min( _surfaceSize.width - _baseCache.getWidth() * _scale , 0 );
                            final float minOffsetY =
                                    Math.min( _surfaceSize.height - _baseCache.getHeight() * _scale , 0 );
                            if ( _offset.x < minOffsetX || _offset.y < minOffsetY || _offset.x > 0 || _offset.y > 0
                                    || _scale < _minScale || _scale > _maxScale ) {
                                final boolean doMinOffsetX = _offset.x < minOffsetX;
                                final boolean doMinOffsetY = _offset.y < minOffsetY;
                                final boolean doMaxOffsetX = _offset.x > 0;
                                final boolean doMaxOffsetY = _offset.y > 0;
                                final boolean doMinScale = _scale < _minScale;
                                final boolean doMaxScale = _scale > _maxScale;

                                final long t = SystemClock.elapsedRealtime();

                                final Interpolator i = new EaseOutBounceInterpolator();

                                final PointF offset = new PointF( _offset.x , _offset.y );
                                final float scale = _scale;

                                while ( !_willCancelCleanUp ) {
                                    final float diff =
                                            Math.min( 1f * ( SystemClock.elapsedRealtime() - t ) / DURATION_CLEAN_UP ,
                                                    1f );

                                    final Canvas c_ = holder.lockCanvas();

                                    if ( doMinOffsetX ) {
                                        _offset.x = offset.x + ( minOffsetX - offset.x ) * i.getInterpolation( diff );
                                    }

                                    if ( doMinOffsetY ) {
                                        _offset.y = offset.y + ( minOffsetY - offset.y ) * i.getInterpolation( diff );
                                    }

                                    if ( doMaxOffsetX ) {
                                        _offset.x = offset.x + ( 0 - offset.x ) * i.getInterpolation( diff );
                                    }

                                    if ( doMaxOffsetY ) {
                                        _offset.y = offset.y + ( 0 - offset.y ) * i.getInterpolation( diff );
                                    }

                                    // TODO change offset (can consider scale clean-up not occur with offset?)
                                    if ( doMinScale ) {
                                        _scale = scale + ( _minScale - scale ) * i.getInterpolation( diff );
                                    }

                                    if ( doMaxScale ) {
                                        _scale = scale + ( _maxScale - scale ) * i.getInterpolation( diff );
                                    }

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

    private float surfaceToImageValue( final float value ) {
        return value / _surfaceSize.width * _sourceSize.width / _baseSampleSize;
    }

    public void translate( final PointF delta ) {
        _offset.offset( delta.x , delta.y );

        _invalidated = true;
    }
}
