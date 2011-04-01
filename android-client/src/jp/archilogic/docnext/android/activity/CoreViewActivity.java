package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.bookmark.BookmarkActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.PageSettable;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.service.DownloadService;
import jp.archilogic.docnext.android.thumnail.ThumnailActivity;
import jp.archilogic.docnext.android.util.AnimationUtils2;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CoreViewActivity extends Activity implements CoreViewDelegate {
    public static final String EXTRA_IDS = "jp.archilogic.docnext.android.activity.CoreViewActivity.ids";
    public static final String EXTRA_PAGE = "page";
    public static final String EXTRA_CURRENT_PAGE = "current_page";

    private static final int REQUEST_PAGE = 1;

    private final CoreViewActivity _self = this;

    private ViewGroup _rootViewGroup;
    private CoreView _view;
    private View _menuView;

    private long[] _ids;

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
            // hack for this method called before ACTION_UP (actually invoked by ACTION_DOWN)
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
            // hack for this method called before ACTION_UP (actually invoked by ACTION_DOWN)
            _view.onGestureEnd();
            _view.onTapGesture( new PointF( e.getX() , e.getY() ) );

            AnimationUtils2.toggle( _self , _menuView );

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

    private View buildCoreViewSwitchMenu( final DocumentType[] types ) {
        final LinearLayout ret = new LinearLayout( _self );
        ret.setLayoutParams( new FrameLayout.LayoutParams( FrameLayout.LayoutParams.FILL_PARENT ,
                FrameLayout.LayoutParams.WRAP_CONTENT ) );
        ret.setPadding( dp( 10 ) , dp( 10 ) , dp( 10 ) , dp( 10 ) );
        ret.setBackgroundColor( 0x80000000 );
        ret.setClickable( true );
        ret.setVisibility( View.GONE );

        for ( final DocumentType type : types ) {
            final Button button = new Button( _self );

            button.setText( type.toString() );
            button.setOnClickListener( new OnClickListener() {
                @Override
                public void onClick( final View v ) {
                    changeCoreViewType( type , new Intent() );
                }
            } );

            ret.addView( button );
        }

        return ret;
    }

    public IntentFilter buildRemoteProviderReceiverFilter() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_PROGRESS );

        return filter;
    }

    @Override
    public void changeCoreViewType( final DocumentType type , final Intent extra ) {
        _rootViewGroup.removeView( ( View ) _view );

        _view = type.buildView( _self );

        _rootViewGroup.addView( ( View ) _view );

        _view.setIds( _ids );
        _view.setDelegate( _self );

        if ( _view instanceof PageSettable && extra.hasExtra( EXTRA_PAGE ) ) {
            ( ( PageSettable ) _view ).setPage( extra.getIntExtra( EXTRA_PAGE , -1 ) );
        }

        _menuView.setVisibility( View.GONE );

        // TODO hack :( Change to use content holder for CoreView
        _rootViewGroup.removeView( _menuView );
        _rootViewGroup.addView( _menuView );
    }

    private boolean contains( final DocumentType[] types , final DocumentType type ) {
        for ( final DocumentType o : types ) {
            if ( o == type ) {
                return true;
            }
        }

        return false;
    }

    private int dp( final float value ) {
        final float density = getResources().getDisplayMetrics().density;

        return Math.round( value * density );
    }

    @Override
    public void onActivityResult( final int requestCode , final int resultCode , final Intent data ) {
        switch ( requestCode ) {
        case REQUEST_PAGE:
            if ( resultCode == Activity.RESULT_OK ) {
                // final int page = data.getExtras().getInt( EXTRA_PAGE );
                // _view.setPage( page );
            }

            break;
        }
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

        _view = validateCoreViewType( _ids ).buildView( _self );

        _rootViewGroup.addView( ( View ) _view );

        _view.setIds( _ids );
        _view.setDelegate( _self );

        _gestureDetector = new GestureDetector( _self , _gestureListener );
        _gestureDetector.setOnDoubleTapListener( _doubleTapListener );
        _scaleGestureDetector = new ScaleGestureDetectorWrapper( _self , _scaleGestureListener );

        _rootViewGroup.addView( _menuView =
                buildCoreViewSwitchMenu( new DocumentType[] { DocumentType.IMAGE , DocumentType.TEXT } ) );
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu menu ) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.document_menu , menu );
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
    }

    @Override
    public boolean onOptionsItemSelected( final MenuItem item ) {
        Intent intent = null;
        switch ( item.getItemId() ) {
        case R.id.table_of_contents_item:
            intent = new Intent( this , TableOfContentsActivity.class );
            intent.putExtra( EXTRA_IDS , getIntent().getLongArrayExtra( EXTRA_IDS ) );
            startActivityForResult( intent , REQUEST_PAGE );
            return true;
        case R.id.thumnail_item:
            intent = new Intent( this , ThumnailActivity.class );
            intent.putExtra( EXTRA_IDS , getIntent().getLongArrayExtra( EXTRA_IDS ) );
            startActivityForResult( intent , REQUEST_PAGE );
            return true;
        case R.id.bookmark_item:
            intent = new Intent( this , BookmarkActivity.class );
            intent.putExtra( EXTRA_IDS , getIntent().getLongArrayExtra( EXTRA_IDS ) );
            // intent.putExtra( EXTRA_CURRENT_PAGE , _view.getCurrentPage() );
            startActivityForResult( intent , REQUEST_PAGE );
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        _view.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _view.onResume();
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

        _scaleGestureDetector.onTouchEvent( event );
        _gestureDetector.onTouchEvent( event );

        return true;
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
