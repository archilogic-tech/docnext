package jp.archilogic.docnext.android.widget;

import java.util.List;

import jp.archilogic.docnext.android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class CoreImageView extends SurfaceView {
    private CoreImageCallback _callback;

    private int _nTouch;
    private final PointF[] _points = new PointF[ 2 ]; // supported multi-touch count

    public CoreImageView( final Context context , final AttributeSet attrs ) {
        super( context , attrs );
    }

    private PointF copyPoint( final PointF point ) {
        return new PointF( point.x , point.y );
    }

    private void copyPoints( final MotionEvent event ) {
        for ( int index = 0 ; index < event.getPointerCount() ; index++ ) {
            _points[ index ] = new PointF( event.getX( index ) , event.getY( index ) );
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final BitmapDrawable backgroundDrawable = ( BitmapDrawable ) getResources().getDrawable( R.drawable.background );
        final Bitmap background = backgroundDrawable.getBitmap();

        getHolder().addCallback( _callback = new CoreImageCallback( background ) );
    }

    @Override
    public boolean onTouchEvent( final MotionEvent event ) {
        switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
        case MotionEvent.ACTION_DOWN: {
            _nTouch = event.getPointerCount();

            if ( _nTouch == 1 ) {
                _callback.cancelCleanUp();
                copyPoints( event );
            }

            break;
        }
        case MotionEvent.ACTION_POINTER_DOWN: {
            _nTouch = event.getPointerCount();

            if ( _nTouch == 2 ) {
                copyPoints( event );
            }

            break;
        }
        case MotionEvent.ACTION_MOVE: {
            if ( _nTouch == 1 ) {
                _callback.translate( new PointF( event.getX() - _points[ 0 ].x , event.getY() - _points[ 0 ].y ) );

                copyPoints( event );
            } else if ( _nTouch == 2 ) {
                final PointF prev0 = copyPoint( _points[ 0 ] );
                final PointF prev1 = copyPoint( _points[ 1 ] );

                copyPoints( event );

                _callback.pinch( prev0 , prev1 , copyPoint( _points[ 0 ] ) , copyPoint( _points[ 1 ] ) );
            }

            break;
        }
        case MotionEvent.ACTION_UP: {
            if ( _nTouch == 1 ) {
                _callback.doCleanUp();

                _nTouch = 0;
            }

            break;
        }
        case MotionEvent.ACTION_POINTER_UP: {
            if ( _nTouch == 2 ) {
                _callback.doCleanUp();

                _nTouch = 0;
            }

            break;
        }
        }

        return true;
    }

    public void setSources( final List< String > sources ) {
        _callback.setSources( sources );
    }
}
