package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.NavigationView;
import jp.archilogic.docnext.android.coreview.NeedCleanup;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.service.DownloadService;
import jp.archilogic.docnext.android.util.AnimationUtils2;
import jp.archilogic.docnext.android.widget.CoreViewMenu;
import jp.archilogic.docnext.android.widget.CoreViewMenu.CoreViewMenuDelegate;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

public class CoreViewActivity extends Activity implements CoreViewDelegate , CoreViewMenuDelegate {
    public static final String EXTRA_IDS =
            "jp.archilogic.docnext.android.activity.CoreViewActivity.ids";
    public static final String EXTRA_PAGE = "page";

    private static final String STATE_TYPE = "type";

    private CoreViewActivity _self = this;

    private ViewGroup _rootViewGroup;
    private CoreView _view;
    private CoreViewMenu _menu;
    private DocumentType _rootType;

    private long[] _ids;
    private DocumentType _type;

    private GestureDetector _gestureDetector;
    private ScaleGestureDetectorWrapper _scaleGestureDetector;
    private boolean _isJustAfterScale = false;

    private final OnGestureListener _gestureListener = new OnGestureListener() {
        @Override
        public boolean onDown( final MotionEvent e ) {
            return false;
        }

        @Override
        public boolean onFling( final MotionEvent e1 , final MotionEvent e2 ,
                final float velocityX , final float velocityY ) {
            if ( _isJustAfterScale ) {
                _isJustAfterScale = false;
            } else {
                _view.onFlingGesture( new PointF( velocityX , velocityY ) );
            }

            return true;
        }

        @Override
        public void onLongPress( final MotionEvent e ) {
            toggleMenu();
        }

        @Override
        public boolean onScroll( final MotionEvent e1 , final MotionEvent e2 ,
                final float distanceX , final float distanceY ) {
            if ( _isJustAfterScale ) {
                _isJustAfterScale = false;
            } else {
                _view.onDragGesture( new PointF( distanceX , distanceY ) );
            }

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
            if ( _isJustAfterScale ) {
                _isJustAfterScale = false;
            } else {
                // hack for this method called before ACTION_UP (actually invoked by ACTION_DOWN)
                _view.onGestureEnd();
                _view.onDoubleTapGesture( new PointF( e.getX() , e.getY() ) );
            }

            return true;
        }

        @Override
        public boolean onDoubleTapEvent( final MotionEvent e ) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed( final MotionEvent e ) {
            if ( _isJustAfterScale ) {
                _isJustAfterScale = false;
            } else {
                // hack for this method called before ACTION_UP (actually invoked by ACTION_DOWN)
                _view.onGestureEnd();
                _view.onTapGesture( new PointF( e.getX() , e.getY() ) );
            }

            return true;
        }
    };

    private final OnScaleGestureWrapperListener _scaleGestureListener =
            new OnScaleGestureWrapperListener() {
                @Override
                public boolean onScale( final ScaleGestureDetectorWrapper detector ) {
                    _view.onZoomGesture( detector.getScaleFactor() ,
                            new PointF( detector.getFocusX() , detector.getFocusY() ) );

                    return true;
                }

                @Override
                public boolean onScaleBegin( final ScaleGestureDetectorWrapper detector ) {
                    return true;
                }

                @Override
                public void onScaleEnd( final ScaleGestureDetectorWrapper detector ) {
                    _isJustAfterScale = true;
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
            } else if ( intent.getAction().equals( HasPage.BROADCAST_PAGE_CHANGED ) ) {
                _menu.onPageChanged();
                setLastOpendPage();
            }
        }

        private void setLastOpendPage() {
            final int page = ( ( HasPage ) _view ).getPage();
            Kernel.getLocalProvider().setLastOpenedPage( _ids[ 0 ] , page );
        }
    };

    private IntentFilter buildRemoteProviderReceiverFilter() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_PROGRESS );
        filter.addAction( HasPage.BROADCAST_PAGE_CHANGED );

        return filter;
    }

    @Override
    public void changeCoreViewType( final DocumentType type , final Intent extra ) {
        if ( type.isRoot() ) {
            _rootType = null;
        } else if ( _type.isRoot() ) {
            _rootType = _type;
        }

        _type = type;

        if ( _view instanceof NeedCleanup ) {
            ( ( NeedCleanup ) _view ).cleanup();
        }

        _rootViewGroup.removeView( ( View ) _view );

        _view = type.buildView( _self );

        _rootViewGroup.addView( ( View ) _view );

        _view.setIds( _ids );
        _view.setDelegate( _self );

        if ( _view instanceof HasPage && extra.hasExtra( EXTRA_PAGE ) ) {
            ( ( HasPage ) _view ).setPage( extra.getIntExtra( EXTRA_PAGE , -1 ) );
        }

        if ( _view instanceof NavigationView ) {
            ( ( NavigationView ) _view ).init();
        }

        // TODO hack :( -- Change to use content holder for CoreView
        _rootViewGroup.removeView( _menu );
        _menu = new CoreViewMenu( _self , type , _ids[ 0 ] , _self );
        _rootViewGroup.addView( _menu );
    }
    
    private void confirmRestorePage() {
        final OnClickListener yesClick = new OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog , final int which ) {
                int page = Kernel.getLocalProvider().getLastOpenedPage( _ids[ 0 ] );
                final Intent intent = new Intent();
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , page );
                changeCoreViewType( _type , intent );
            }
        };
        
        new AlertDialog.Builder( _self ).setMessage( R.string.confirm_restore ) 
                .setPositiveButton( R.string.yes , yesClick )
                .setNegativeButton( R.string.no , null ).show();
    }
    

    private void confirmFinish() {
        final OnClickListener yesClick = new OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog , final int which ) {
                finish();
            }
        };

        new AlertDialog.Builder( _self ).setMessage( R.string.confirm_finish )
                .setPositiveButton( R.string.yes , yesClick )
                .setNegativeButton( R.string.no , null ).show();
    }

    private boolean contains( final DocumentType[] types , final DocumentType type ) {
        for ( final DocumentType o : types ) {
            if ( o == type ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CoreView getCoreView() {
        return _view;
    }

    @Override
    public void goBack() {
        if ( _rootType == null ) {
            confirmFinish();

            return;
        }

        changeCoreViewType( _rootType , new Intent() );
        _rootType = null;
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        _rootViewGroup = new FrameLayout( _self );

        setContentView( _rootViewGroup );

        _ids = getIntent().getLongArrayExtra( EXTRA_IDS );
        if ( _ids == null || _ids.length == 0 ) {
            throw new RuntimeException();
        }

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        _type = validateCoreViewType( _ids );
        
        _view = _type.buildView( _self );

        _rootViewGroup.addView( ( View ) _view );

        _view.setIds( _ids );
        _view.setDelegate( _self );

        _menu = new CoreViewMenu( _self , _type , _ids[ 0 ] , _self );
        _rootViewGroup.addView( _menu );

        _gestureDetector = new GestureDetector( _self , _gestureListener );
        _gestureDetector.setOnDoubleTapListener( _doubleTapListener );
        _scaleGestureDetector = new ScaleGestureDetectorWrapper( _self , _scaleGestureListener );

        confirmRestorePage();
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu menu ) {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( _view instanceof NeedCleanup ) {
            ( ( NeedCleanup ) _view ).cleanup();
        }

        _self = null;
        _rootViewGroup = null;
        _view = null;
        _menu = null;
        _rootType = null;
        _gestureDetector = null;
        _scaleGestureDetector = null;

        unregisterReceiver( _remoteProviderReceiver );

        System.gc();
    }

    @Override
    public boolean onKeyDown( final int keyCode , final KeyEvent event ) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            if ( _menu.getVisibility() != View.GONE ) {
                toggleMenu();
            } else {
                goBack();
            }

            return true;
        }

        return super.onKeyDown( keyCode , event );
    }

    @Override
    protected void onPause() {
        super.onPause();

        _view.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu( final Menu menu ) {
        toggleMenu();

        return true;
    }

    @Override
    protected void onRestoreInstanceState( final Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState );

        changeCoreViewType( ( DocumentType ) savedInstanceState.getSerializable( STATE_TYPE ) ,
                new Intent() );

        _view.restoreState( savedInstanceState );
    }

    @Override
    protected void onResume() {
        super.onResume();

        _view.onResume();
    }

    @Override
    protected void onSaveInstanceState( final Bundle outState ) {
        super.onSaveInstanceState( outState );

        outState.putSerializable( STATE_TYPE , _type );

        _view.saveState( outState );
    }

    @Override
    public boolean onTouchEvent( final MotionEvent event ) {
        switch ( event.getAction() ) {
        case MotionEvent.ACTION_DOWN:
            _view.onGestureBegin();
            break;
        case MotionEvent.ACTION_UP:
            _view.onGestureEnd();
            break;
        }

        // translate to _view location
        final View v = getWindow().findViewById( Window.ID_ANDROID_CONTENT );

        event.offsetLocation( -v.getLeft() , -v.getTop() );

        if ( _scaleGestureDetector.isInProgress() ) {
            _scaleGestureDetector.onTouchEvent( event );
        } else {
            _scaleGestureDetector.onTouchEvent( event );
            _gestureDetector.onTouchEvent( event );
        }

        return true;
    }

    private void toggleMenu() {
        final boolean willVisible = _menu.getVisibility() == View.GONE;

        AnimationUtils2.toggle( _self , _menu );

        _view.onMenuVisibilityChange( willVisible );
    }

    private DocumentType validateCoreViewType( final long[] ids ) {
        DocumentType ret = null;

        for ( final long id : ids ) {
            final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

            if ( ret == null ) {
                ret = doc.types[ 0 ];
            } else if ( !contains( doc.types , ret ) ) {
                throw new RuntimeException();
            }
        }

        return ret;
    }
}
