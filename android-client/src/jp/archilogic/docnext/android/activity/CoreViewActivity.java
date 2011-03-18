package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.CoreViewType;
import jp.archilogic.docnext.android.provider.remote.RemoteProvider;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

public class CoreViewActivity extends Activity implements CoreViewDelegate {
    public static final String EXTRA_IDS = "jp.archilogic.docnext.android.activity.CoreViewActivity.ids";

    private static final float TAP_THREASHOLD = 10;

    private ViewGroup _rootViewGroup;
    private CoreView _view;

    private int _nTouch;
    private final PointF[] _prevPoints = new PointF[ 2 ]; // supported multi-touch count
    private final PointF[] _downPoints = new PointF[ 2 ];
    private boolean _isTap;
    private PointF _zoomCenter;

    private final BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED ) ) {
                final long id = intent.getLongExtra( RemoteProvider.EXTRA_ID , -1 );
                final int page = intent.getIntExtra( RemoteProvider.EXTRA_PAGE , -1 );
                // final int level = intent.getIntExtra( RemoteProvider.EXTRA_LEVEL , -1 );
                // final int px = intent.getIntExtra( RemoteProvider.EXTRA_PX , -1 );
                // final int py = intent.getIntExtra( RemoteProvider.EXTRA_PY , -1 );

                final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

                if ( page + 1 < doc.pages ) {
                    setProgress( Window.PROGRESS_END * ( page + 1 ) / doc.pages );
                } else {
                    setProgressBarVisibility( false );
                }
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED ) ) {
            }
        }
    };

    public IntentFilter buildRemoteProviderReceiverFilter() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction( RemoteProvider.BROADCAST_GET_DOC_INFO_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_DOC_INFO_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_FONT_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_FONT_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_IMAGE_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED );

        return filter;
    }

    @Override
    public void changeCoreViewType( final CoreViewType type ) {
        // Not implemented
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
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        _rootViewGroup = new FrameLayout( this );

        setContentView( _rootViewGroup );

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        final long[] ids = getIntent().getLongArrayExtra( EXTRA_IDS );
        if ( ids == null || ids.length == 0 ) {
            throw new RuntimeException();
        }

        _view = validateCoreViewType( ids ).buildView( this );

        _rootViewGroup.addView( ( View ) _view );

        _view.setIds( ids );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
    }

    private boolean onTouchDown( final MotionEvent event ) {
        _view.onGestureBegin();

        _nTouch = event.getPointerCount();

        if ( _nTouch == 1 ) {
            copyPoints( event , _prevPoints );
            copyPoints( event , _downPoints );

            _isTap = true;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent( final MotionEvent event ) {
        if ( event.getPointerCount() > 2 ) {
            return super.onTouchEvent( event );
        }

        switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
        case MotionEvent.ACTION_DOWN:
            return onTouchDown( event );
        case MotionEvent.ACTION_POINTER_DOWN:
            return onTouchPointerDown( event );
        case MotionEvent.ACTION_MOVE:
            return onTouchMove( event );
        case MotionEvent.ACTION_UP:
            return onTouchUp();
        case MotionEvent.ACTION_POINTER_UP:
            return onTouchPointerUp();
        }

        return super.onTouchEvent( event );
    }

    private boolean onTouchMove( final MotionEvent event ) {
        if ( _nTouch == 1 ) {
            if ( _isTap
                    && Math.hypot( event.getX() - _downPoints[ 0 ].x , event.getY() - _downPoints[ 0 ].y ) > TAP_THREASHOLD ) {
                _isTap = false;
            }

            if ( !_isTap ) {
                _view.onDragGesture( new PointF( event.getX() - _prevPoints[ 0 ].x , event.getY() - _prevPoints[ 0 ].y ) );

                copyPoints( event , _prevPoints );
            }
        } else if ( _nTouch == 2 ) {
            final PointF prev0 = copyPoint( _prevPoints[ 0 ] );
            final PointF prev1 = copyPoint( _prevPoints[ 1 ] );

            copyPoints( event , _prevPoints );

            final float prevDist = ( float ) Math.hypot( prev0.x - prev1.x , prev0.y - prev1.y );
            final float curDist = ( float ) Math.hypot( _prevPoints[ 0 ].x - _prevPoints[ 1 ].x , //
                    _prevPoints[ 0 ].y - _prevPoints[ 1 ].y );

            _view.onZoomGesture( curDist / prevDist , _zoomCenter );
        }

        return true;
    }

    private boolean onTouchPointerDown( final MotionEvent event ) {
        _nTouch = event.getPointerCount();

        if ( _nTouch == 2 ) {
            copyPoints( event , _prevPoints );

            _zoomCenter = new PointF( ( _prevPoints[ 0 ].x + _prevPoints[ 1 ].x ) / 2 , //
                    ( _prevPoints[ 0 ].y + _prevPoints[ 1 ].y ) / 2 );
        }

        return true;
    }

    private boolean onTouchPointerUp() {
        if ( _nTouch == 2 ) {
            _nTouch = 0;
        }

        return true;
    }

    private boolean onTouchUp() {
        if ( _nTouch == 1 ) {
            if ( _isTap ) {
                _view.onTapGesture( _downPoints[ 0 ] );
            }

            _nTouch = 0;
        }

        _view.onGestureEnd();

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
