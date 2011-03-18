package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;

public class CoreImageView extends GLSurfaceView implements CoreView {
    private CoreImageRenderer _renderer;

    public CoreImageView( final Context context ) {
        super( context );

        setRenderer( _renderer = new CoreImageRenderer( context ) );
        _renderer.setDirection( CoreImageDirection.R2L );
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
    }

    @Override
    public void onDragGesture( final PointF delta ) {
        _renderer.drag( delta );
    }

    @Override
    public void onGestureBegin() {
        _renderer.beginInteraction();
    }

    @Override
    public void onGestureEnd() {
        _renderer.endInteraction();
    }

    @Override
    public void onTapGesture( final PointF point ) {
        if ( point.x < 50 && point.y < 50 ) {
            _renderer.setDirection( CoreImageDirection.values()[ ( _renderer.getDirection().ordinal() + 1 )
                    % CoreImageDirection.values().length ] );
        }
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
        _renderer.zoom( scaleDelta , center );
    }

    @Override
    public void setDelegate( final CoreViewDelegate delegate ) {
    }

    @Override
    public void setIds( final long[] ids ) {
        _renderer.setId( ids[ 0 ] );
    }
}