package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class CoreImageView extends SurfaceView implements CoreView {
    private static final float TAP_THREASHOLD = 10;

    private final CoreImageCallback _callback;

    private int _nTouch;
    private final PointF[] _prevPoints = new PointF[ 2 ]; // supported multi-touch count
    private final PointF[] _downPoints = new PointF[ 2 ];
    private boolean _isTap;
    private PointF _pinchCenter;

    public CoreImageView( final Context context ) {
        super( context );

        final BitmapDrawable backgroundDrawable = ( BitmapDrawable ) getResources().getDrawable( R.drawable.background );
        final Bitmap background = backgroundDrawable.getBitmap();

        _callback = new CoreImageCallback( background );

        getHolder().addCallback( _callback );

        _callback.setDirection( ImageDocDirection.R2L );
    }

    private PointF copyPoint( final PointF point ) {
        return new PointF( point.x , point.y );
    }

    private void copyPoints( final MotionEvent event , final PointF[] dst ) {
        for ( int index = 0 ; index < event.getPointerCount() ; index++ ) {
            dst[ index ] = new PointF( event.getX( index ) , event.getY( index ) );
        }
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragGesture( final PointF delta ) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void onGestureBegin( final PointF point ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGestureEnd( final PointF point ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTapGesture( final PointF point ) {
        // TODO Auto-generated method stub

    }

    private void onTouchDown( final MotionEvent event ) {
        _nTouch = event.getPointerCount();

        if ( _nTouch == 1 ) {
            _callback.cancelCleanUp();

            copyPoints( event , _prevPoints );
            copyPoints( event , _downPoints );
            _isTap = true;
        }
    }

    @Override
    public boolean onTouchEvent( final MotionEvent event ) {
        if ( event.getPointerCount() > 2 ) {
            return true;
        }

        switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
        case MotionEvent.ACTION_DOWN:
            onTouchDown( event );
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            onTouchPointerDown( event );
            break;
        case MotionEvent.ACTION_MOVE:
            onTouchMove( event );
            break;
        case MotionEvent.ACTION_UP:
            onTouchUp();
            break;
        case MotionEvent.ACTION_POINTER_UP:
            onTouchPointerUp();
            break;
        }

        return true;
    }

    private void onTouchMove( final MotionEvent event ) {
        if ( _nTouch == 1 ) {
            if ( _isTap
                    && Math.hypot( event.getX() - _downPoints[ 0 ].x , event.getY() - _downPoints[ 0 ].y ) > TAP_THREASHOLD ) {
                _isTap = false;
            }

            if ( !_isTap ) {
                _callback
                        .translate( new PointF( event.getX() - _prevPoints[ 0 ].x , event.getY() - _prevPoints[ 0 ].y ) );

                copyPoints( event , _prevPoints );
            }
        } else if ( _nTouch == 2 ) {
            final PointF prev0 = copyPoint( _prevPoints[ 0 ] );
            final PointF prev1 = copyPoint( _prevPoints[ 1 ] );

            copyPoints( event , _prevPoints );

            final float prevD = ( float ) Math.hypot( prev0.x - prev1.x , prev0.y - prev1.y );
            final float curD =
                    ( float ) Math.hypot( _prevPoints[ 0 ].x - _prevPoints[ 1 ].x , _prevPoints[ 0 ].y
                            - _prevPoints[ 1 ].y );

            _callback.scale( curD / prevD , _pinchCenter );
        }
    }

    private void onTouchPointerDown( final MotionEvent event ) {
        _nTouch = event.getPointerCount();

        if ( _nTouch == 2 ) {
            copyPoints( event , _prevPoints );

            _pinchCenter =
                    new PointF( ( _prevPoints[ 0 ].x + _prevPoints[ 1 ].x ) / 2 ,
                            ( _prevPoints[ 0 ].y + _prevPoints[ 1 ].y ) / 2 );
        }
    }

    private void onTouchPointerUp() {
        if ( _nTouch == 2 ) {
            _callback.doCleanUp( false );

            _nTouch = 0;
        }
    }

    private void onTouchUp() {
        if ( _nTouch == 1 ) {
            _callback.doCleanUp( true );

            if ( _isTap ) {
                _callback.tap( _downPoints[ 0 ] );
            }

            _nTouch = 0;
        }
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDelegate( final CoreViewDelegate delegate ) {
        // Not implemented
    }

    public void setDirection( final ImageDocDirection d ) {
        _callback.setDirection( d );
    }

    @Override
    public void setIds( final long[] ids ) {
        _callback.setIds( ids );
    }
}
