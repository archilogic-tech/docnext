package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.CoreViewType;
import jp.archilogic.docnext.android.service.DownloadService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

public class CoreViewActivity extends Activity implements CoreViewDelegate {
    public static final String EXTRA_IDS = "jp.archilogic.docnext.android.activity.CoreViewActivity.ids";

	private static final int REQUEST_PAGE = 1;

    private final CoreViewActivity _self = this;

    private ViewGroup _rootViewGroup;
    private CoreView _view;

    private GestureDetector _gestureDetector;
    private ScaleGestureDetectorWrapper _scaleGestureDetector;

    private final OnGestureListener _gestureListener = new OnGestureListener() {
        @Override
        public boolean onDown( final MotionEvent e ) {
            return false;
        }

        @Override
        public boolean onFling( final MotionEvent e1 , final MotionEvent e2 , final float velocityX ,
                final float velocityY ) {
            return false;
        }

        @Override
        public void onLongPress( final MotionEvent e ) {
        }

        @Override
        public boolean onScroll( final MotionEvent e1 , final MotionEvent e2 , final float distanceX ,
                final float distanceY ) {
            _view.onDragGesture( new PointF( distanceX , distanceY ) );

            return true;
        }

        @Override
        public void onShowPress( final MotionEvent e ) {
        }

        @Override
        public boolean onSingleTapUp( final MotionEvent e ) {
            return false;
        }
    };

    private final OnDoubleTapListener _doubleTapListener = new OnDoubleTapListener() {
        @Override
        public boolean onDoubleTap( final MotionEvent e ) {
            // hack for this method called before ACTION_UP (actually invoked ACTION_DOWN)
            _view.onGestureEnd();
            _view.onDoubleTapGesture( new PointF( e.getX() , e.getY() ) );

            return true;
        }

        @Override
        public boolean onDoubleTapEvent( final MotionEvent e ) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed( final MotionEvent e ) {
            // hack for this method called before ACTION_UP (actually invoked ACTION_DOWN)
            _view.onGestureEnd();
            _view.onTapGesture( new PointF( e.getX() , e.getY() ) );

            return true;
        }
    };

    private final OnScaleGestureWrapperListener _scaleGestureListener = new OnScaleGestureWrapperListener() {
        @Override
        public boolean onScale( final ScaleGestureDetectorWrapper detector ) {
            _view.onZoomGesture( detector.getScaleFactor() , new PointF( detector.getFocusX() , detector.getFocusY() ) );

            return true;
        }

        @Override
        public boolean onScaleBegin( final ScaleGestureDetectorWrapper detector ) {
            return true;
        }

        @Override
        public void onScaleEnd( final ScaleGestureDetectorWrapper detector ) {
        }
    };

    private final BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            if ( intent.getAction().equals( DownloadService.BROADCAST_DOWNLOAD_PROGRESS ) ) {
                final int current = intent.getIntExtra( DownloadService.EXTRA_CURRENT , -1 );
                final int total = intent.getIntExtra( DownloadService.EXTRA_TOTAL , -1 );

                if ( current < total ) {
                    setProgress( Window.PROGRESS_END * current / total );
                } else {
                    setProgressBarVisibility( false );
                }
            }
        }
    };

    public IntentFilter buildRemoteProviderReceiverFilter() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_PROGRESS );

        return filter;
    }

    @Override
    public void changeCoreViewType( final CoreViewType type ) {
        // Not implemented
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case REQUEST_PAGE:
    		if (resultCode == Activity.RESULT_OK) {
    			int page = data.getExtras().getInt(TableOfContentsActivity.EXTRA_PAGE);
    			Log.d("docnext", "onActivityResult: page:" + page);
    			// TODO: change page
    		}
    		break;
    	}
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        _rootViewGroup = new FrameLayout( this );

        setContentView( _rootViewGroup );

        final long[] ids = getIntent().getLongArrayExtra( EXTRA_IDS );
        if ( ids == null || ids.length == 0 ) {
            throw new RuntimeException();
        }

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        _view = validateCoreViewType( ids ).buildView( this );

        _rootViewGroup.addView( ( View ) _view );

        _view.setIds( ids );

        _gestureDetector = new GestureDetector( _self , _gestureListener );
        _gestureDetector.setOnDoubleTapListener( _doubleTapListener );
        _scaleGestureDetector = new ScaleGestureDetectorWrapper( _self , _scaleGestureListener );
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.document_menu, menu);
    	return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent = null;
    	switch (item.getItemId()) {
    	case R.id.table_of_contents_item:
    		intent = new Intent(this, TableOfContentsActivity.class);
    		startActivityForResult(intent, REQUEST_PAGE);
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean onTouchEvent( final MotionEvent event ) {
        // TODO need research
        switch ( event.getAction() /* & MotionEvent.ACTION_MASK */) {
        case MotionEvent.ACTION_DOWN:
            _view.onGestureBegin();
            break;
        case MotionEvent.ACTION_UP:
            _view.onGestureEnd();
            break;
        }

        _scaleGestureDetector.onTouchEvent( event );
        _gestureDetector.onTouchEvent( event );

        return true;
    }

    private CoreViewType validateCoreViewType( final long[] ids ) {
        CoreViewType ret = null;

        for ( final long id : ids ) {
            final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

            if ( ret == null ) {
                ret = doc.type;
            } else if ( ret != doc.type ) {
                throw new RuntimeException();
            }
        }

        return ret;
    }
}
