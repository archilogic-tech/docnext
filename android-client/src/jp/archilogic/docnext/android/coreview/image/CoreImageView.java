package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.image.CoreImageState.OnScaleChangeListener;
import jp.archilogic.docnext.android.util.AnimationUtils2;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class CoreImageView extends FrameLayout implements CoreView , HasPage {
    private static final boolean DEBUG = false;

    private GLSurfaceView _glSurfaceView;
    private View _menuView;
    private View _l2rButton;
    private View _r2lButton;
    private View _t2bButton;
    private View _b2tButton;

    private CoreImageRenderer _renderer;

    private ZoomButtonsController _zoomButtonsController = null;

    private final OnClickListener _l2rButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _renderer.setDirection( CoreImageDirection.L2R );
        }
    };

    private final OnClickListener _r2lButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _renderer.setDirection( CoreImageDirection.R2L );
        }
    };

    private final OnClickListener _t2bButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _renderer.setDirection( CoreImageDirection.T2B );
        }
    };

    private final OnClickListener _b2tButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _renderer.setDirection( CoreImageDirection.B2T );
        }
    };

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

        LayoutInflater.from( context ).inflate( R.layout.core_image , this , true );

        assignWidget();

        _glSurfaceView.setRenderer( _renderer = new CoreImageRenderer( context ) );
        _renderer.setDirection( CoreImageDirection.R2L );

        if ( Build.VERSION.SDK_INT < 8 || true ) {
            _renderer.setOnScaleChangeListener( _scaleChangeListener );

            _zoomButtonsController = new ZoomButtonsController( this );
            _zoomButtonsController.setOnZoomListener( _zoomButtonsControllerZoom );
        }

        _l2rButton.setOnClickListener( _l2rButtonClick );
        _r2lButton.setOnClickListener( _r2lButtonClick );
        _t2bButton.setOnClickListener( _t2bButtonClick );
        _b2tButton.setOnClickListener( _b2tButtonClick );
    }

    private void assignWidget() {
        _glSurfaceView = ( GLSurfaceView ) findViewById( R.id.glSurfaceView );
        _menuView = findViewById( R.id.menuView );
        _l2rButton = findViewById( R.id.l2rButton );
        _r2lButton = findViewById( R.id.r2lButton );
        _t2bButton = findViewById( R.id.t2bButton );
        _b2tButton = findViewById( R.id.b2tButton );
    }

    @Override
    public int getPage() {
        return _renderer.getCurrentPage();
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
    public void onFlingGesture( final PointF velocity ) {
        _renderer.fling( velocity );
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
    public void onPause() {
        _glSurfaceView.onPause();

        if ( DEBUG ) {
            Debug.stopMethodTracing();
        }
    }

    @Override
    public void onResume() {
        if ( DEBUG ) {
            Debug.startMethodTracing();
        }

        _glSurfaceView.onResume();
    }

    @Override
    public void onTapGesture( final PointF point ) {
        AnimationUtils2.toggle( getContext() , _menuView );
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
