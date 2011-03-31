package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.image.CoreImageEngine.OnScaleChangeListener;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class CoreImageView extends GLSurfaceView implements CoreView {
    private CoreImageRenderer _renderer;

    private ZoomButtonsController _zoomButtonsController = null;

    private final OnZoomListener _zoomButtonsControllerZoom = new OnZoomListener() {
        @Override
        public void onVisibilityChanged( final boolean visible ) {
        }

        @Override
        public void onZoom( final boolean zoomIn ) {
            _renderer.zoomByLevel( zoomIn ? 1 : -1 );
        }
    };

    private final OnScaleChangeListener _scaleChangeListener = new OnScaleChangeListener() {
        @Override
        public void onScaleChange( final boolean isMin , final boolean isMax ) {
            _zoomButtonsController.setZoomOutEnabled( !isMin );
            _zoomButtonsController.setZoomInEnabled( !isMax );
        }
    };

    public CoreImageView( final Context context ) {
        super( context );

        setRenderer( _renderer = new CoreImageRenderer( context ) );
        _renderer.setDirection( CoreImageDirection.R2L );

        if ( Build.VERSION.SDK_INT < 8 || true ) {
            _renderer.setOnScaleChangeListener( _scaleChangeListener );

            _zoomButtonsController = new ZoomButtonsController( this );
            _zoomButtonsController.setOnZoomListener( _zoomButtonsControllerZoom );
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if ( _zoomButtonsController != null ) {
            _zoomButtonsController.setVisible( false );
        }
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
        _renderer.doubleTap( point );
    }

    @Override
    public void onDragGesture( final PointF delta ) {
        _renderer.drag( delta );
    }

    @Override
    public void onGestureBegin() {
        _renderer.beginInteraction();

        if ( _zoomButtonsController != null ) {
            _zoomButtonsController.setVisible( true );
        }
    }

    @Override
    public void onGestureEnd() {
        _renderer.endInteraction();
    }

    @Override
    public void onTapGesture( final PointF point ) {
        if ( point.x < 100 && point.y < 100 ) {
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

    @Override
    public void setPage( final int page ) {
        _renderer.setPage( page );
    }
}
