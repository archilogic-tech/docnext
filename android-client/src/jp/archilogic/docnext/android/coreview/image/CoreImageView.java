package jp.archilogic.docnext.android.coreview.image;


import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.NeedCleanup;
import jp.archilogic.docnext.android.coreview.image.CoreImageState.OnScaleChangeListener;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.service.DownloadService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class CoreImageView extends FrameLayout implements CoreView , HasPage , NeedCleanup {
    private static final boolean DEBUG = false;

    private static final String STATE_PAGE = "page";

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

    private ProgressBar _downloadIndicator;
    
    private BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            if ( intent.getAction().equals( DownloadService.BROADCAST_DOWNLOAD_DOWNLOADED) ) {
                int page = intent.getIntExtra( DownloadService.EXTRA_PAGE , -1 );
                
                if ( page == getPage() ) {
                    toggleDownloadIndicator();
                }
            } else if ( intent.getAction().equals( HasPage.BROADCAST_PAGE_CHANGED ) ) {
                toggleDownloadIndicator();
            }
        }
    };

    public CoreImageView( final Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.core_image , this , true );

        assignWidget();

        _glSurfaceView.setDebugFlags( GLSurfaceView.DEBUG_CHECK_GL_ERROR
                | GLSurfaceView.DEBUG_LOG_GL_CALLS );

        _glSurfaceView.setRenderer( _renderer =
                new CoreImageRenderer( context.getApplicationContext() ) );
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
        
        IntentFilter filter = new IntentFilter();
        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_DOWNLOADED );
        filter.addAction( HasPage.BROADCAST_PAGE_CHANGED );
        getContext().registerReceiver( _remoteProviderReceiver , filter );
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
    public void cleanup() {
        getContext().unregisterReceiver( _remoteProviderReceiver );

        _glSurfaceView = null;
        _menuView = null;
        _l2rButton = null;
        _r2lButton = null;
        _t2bButton = null;
        _b2tButton = null;
        _renderer.cleanup();
        _renderer = null;
    }

    private int dip( final float value ) {
        return ( int ) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                (float) value , getResources().getDisplayMetrics());
    }

    @Override
    public int getPage() {
        return _renderer.getPage();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        _zoomButtonsController.setVisible( false );
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
    public void onMenuVisibilityChange( final boolean isMenuVisible ) {
        final boolean willVisible = _menuView.getVisibility() == GONE;

        if ( isMenuVisible == willVisible ) {
            //AnimationUtils2.toggle( getContext() , _menuView );
        }
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
        _renderer.tap( point );
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
        _renderer.zoom( scaleDelta , center );
    }

    @Override
    public void restoreState( final Bundle state ) {
        _renderer.setPage( state.getInt( STATE_PAGE ) );
    }

    @Override
    public void saveState( final Bundle state ) {
        state.putInt( STATE_PAGE , _renderer.getPage() );
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
    
    private void toggleDownloadIndicator() {
        if ( _downloadIndicator != null ) {
            removeView( _downloadIndicator );
        }
        
        int page = getPage();

        long id = _renderer.getId();
        DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );
        
        if ( page <= doc.pages && 
                !Kernel.getLocalProvider().isAllImageExists( id , page ) ) {

            _downloadIndicator = new ProgressBar( getContext() );
            _downloadIndicator.setLayoutParams( new FrameLayout.LayoutParams(
                    dip( 50 ) , dip( 50 ) , Gravity.CENTER ) );
            addView( _downloadIndicator );
        }
    }
}
