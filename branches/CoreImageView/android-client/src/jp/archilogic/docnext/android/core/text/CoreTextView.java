package jp.archilogic.docnext.android.core.text;

import jp.archilogic.docnext.android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class CoreTextView extends SurfaceView {
    private CoreTextCallback _callback;

    private final PointF[] _prevPoints = new PointF[ 2 ]; // supported multi-touch count

    public CoreTextView( final Context context , final AttributeSet attrs ) {
        super( context , attrs );
    }

    private void copyPoints( final MotionEvent event , final PointF[] dst ) {
        for ( int index = 0 ; index < event.getPointerCount() ; index++ ) {
            dst[ index ] = new PointF( event.getX( index ) , event.getY( index ) );
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final BitmapDrawable backgroundDrawable = ( BitmapDrawable ) getResources().getDrawable( R.drawable.background );
        final Bitmap background = backgroundDrawable.getBitmap();

        getHolder().addCallback( _callback = new CoreTextCallback( background ) );
    }

    private void onTouchDown( final MotionEvent event ) {
        _callback.cancelCleanUp();

        copyPoints( event , _prevPoints );
    }

    @Override
    public boolean onTouchEvent( final MotionEvent event ) {
        if ( event.getPointerCount() > 1 ) {
            return true;
        }

        switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
        case MotionEvent.ACTION_DOWN:
            onTouchDown( event );
            break;
        case MotionEvent.ACTION_MOVE:
            onTouchMove( event );
            break;
        case MotionEvent.ACTION_UP:
            onTouchUp();
            break;
        }

        return true;
    }

    private void onTouchMove( final MotionEvent event ) {
        _callback.translate( new PointF( event.getX() - _prevPoints[ 0 ].x , event.getY() - _prevPoints[ 0 ].y ) );

        copyPoints( event , _prevPoints );
    }

    private void onTouchUp() {
        _callback.doCleanUp();
    }

    /*
     * public void setListener( final CoreImageListener l ) { _callback.setListener( l ); }
     */
    public void setConfig( final CoreTextConfig c ) {
        _callback.setConfig( c );
    }

    public void setDirection( final TextDocDirection d ) {
        _callback.setDirection( d );
    }

    public void setSources( final String source ) {
        _callback.setSource( source );
    }
}
