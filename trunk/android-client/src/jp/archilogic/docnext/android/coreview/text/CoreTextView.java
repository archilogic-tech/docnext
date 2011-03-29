package jp.archilogic.docnext.android.coreview.text;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceView;

public class CoreTextView extends SurfaceView implements CoreView {
    private CoreTextCallback _callback;

    public CoreTextView( final Context context ) {
        super( context );
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
    }

    @Override
    public void onDragGesture( final PointF delta ) {
        _callback.drag( delta );
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final BitmapDrawable backgroundDrawable = ( BitmapDrawable ) getResources().getDrawable( R.drawable.background );
        final Bitmap background = backgroundDrawable.getBitmap();

        getHolder().addCallback( _callback = new CoreTextCallback( background ) );
    }

    @Override
    public void onGestureBegin() {
        _callback.beginInteraction();
    }

    @Override
    public void onGestureEnd() {
        _callback.endInteraction();
    }

    @Override
    public void onTapGesture( final PointF point ) {
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
    }

    public void setConfig( final CoreTextConfig c ) {
        _callback.setConfig( c );
    }

    @Override
    public void setDelegate( final CoreViewDelegate delegate ) {
    }

    @Override
    public void setIds( final long[] ids ) {
        _callback.setId( ids[ 0 ] );
    }
}
