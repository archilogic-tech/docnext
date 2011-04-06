package jp.archilogic.docnext.android.activity;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.PageSettable;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.service.DownloadService;
import jp.archilogic.docnext.android.util.AnimationUtils2;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CoreViewActivity extends Activity implements CoreViewDelegate {
    public static final String EXTRA_IDS = "jp.archilogic.docnext.android.activity.CoreViewActivity.ids";
    public static final String EXTRA_PAGE = "page";

    private final CoreViewActivity _self = this;

    private ViewGroup _rootViewGroup;
    private CoreView _view;
    private View _menuView;
    private View _bookmarkView;

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
            _view.onFlingGesture( new PointF( velocityX , velocityY ) );

            return true;
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
        final GridView gridView = new GridView( _self );
        gridView.setLayoutParams(  new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT ,
                FrameLayout.LayoutParams.WRAP_CONTENT ) );
        gridView.setBackgroundColor( 0x80000000 );
        gridView.setClickable( true );
        final int NUM_COLUMNS = 5;
        gridView.setNumColumns( NUM_COLUMNS );
        gridView.setVisibility( View.GONE );
        final ArrayList< View > menuItems = new ArrayList< View >();

        for ( final DocumentType type : types ) {
            LinearLayout layout = new LinearLayout( _self );
            layout.setLayoutParams( new GridView.LayoutParams( 
                    GridView.LayoutParams.WRAP_CONTENT ,
                    GridView.LayoutParams.WRAP_CONTENT ) );
            layout.setOrientation( LinearLayout.VERTICAL );

            try {
                int id;
                if ( type == DocumentType.BOOKMARK ) {
                    List< Integer > list = Kernel.getLocalProvider().getBookmarkInfo( _ids[ 0 ] );
                    TreeSet< Integer > set = new TreeSet< Integer >( list );
                    int currentPage = ( ( PageSettable ) _view).getCurrentPage();
                    if ( set.contains( currentPage ) ) {
                        id = R.drawable.button_bookmark_on;
                    } else {
                        id = R.drawable.button_bookmark; 
                    }
                } else {
                    Field idField = 
                        R.drawable.class.getDeclaredField( "button_" + type.toString().toLowerCase() );
                    id = idField.getInt( new R.drawable() );
                }

                ImageView imageView = new ImageView( _self );
                ( ( ImageView )imageView ).setImageBitmap( LoadBitmap( id ) );
                imageView.setLayoutParams( new LinearLayout.LayoutParams( dp( 75 ) , dp( 75 ) ) );
                imageView.setPadding( dp( 10 ) , dp( 10 ) , dp( 10 ) , dp( 10 ) );
                layout.addView( imageView );

                TextView textView = new TextView( _self );
                try {
                    Field field = R.string.class.getDeclaredField( type.toString().toLowerCase() );
                    textView.setText( field.getInt( new R.string() ) );
                } catch (NoSuchFieldException e) {
                    textView.setText( type.toString().toLowerCase() );
                } catch (Exception e) { }
                textView.setGravity( Gravity.CENTER );
                layout.addView( textView );

            } catch (NoSuchFieldException e) {
                final Button button = new Button( _self );
                button.setText( type.toString() );
                layout.addView( button );
            } catch (Exception e) { }

            if ( type == DocumentType.BOOKMARK ) {
                _bookmarkView = layout.getChildAt( 0 );
                layout.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        switchBookmarkIcon();
                    }
                } );
            } else {
                layout.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        changeCoreViewType( type , new Intent() );
                    }
                } );
            }

            if ( type != Kernel.getLocalProvider().getDocInfo( _ids[ 0 ] ).types[ 0 ]) {
                if ( type == DocumentType.SEARCH ) {
                    for ( int i = 0; i < ( NUM_COLUMNS - menuItems.size() % NUM_COLUMNS ); i++ ) {
                        LinearLayout padding = new LinearLayout( _self );
                        menuItems.add( padding );
                    }
                    layout.setGravity( Gravity.LEFT );
                }
                if ( type == DocumentType.HOME ) {
                    menuItems.add( 0, layout );
                } else {
                    menuItems.add( layout );
                }
            }
        }

        gridView.setAdapter( new BaseAdapter() {
            @Override
            public int getCount() {
                return menuItems.size();
            }

            @Override
            public Object getItem( int position ) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getItemId( int position ) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public View getView( int position, View convertView,
                    ViewGroup parent ) {
                return menuItems.get( position );
            }

        });

        return gridView;
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

        // TODO hack :( -- Change to use content holder for CoreView
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

    private Bitmap LoadBitmap( final int id ) {
        final InputStream in = _self.getResources().openRawResource( id );
        final Bitmap bitmap = BitmapFactory.decodeStream( in );
        return bitmap;
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
            buildCoreViewSwitchMenu( new DocumentType[] { 
                    DocumentType.IMAGE , DocumentType.TEXT, DocumentType.TOC , 
                    DocumentType.BOOKMARK , DocumentType.THUMNAIL , DocumentType.HOME , 
                    DocumentType.SETTING , DocumentType.COMMENT , DocumentType.SEARCH } ) );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
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
        if ( _menuView.getVisibility() == View.VISIBLE ) {
            AnimationUtils2.toggle( _self , _menuView );
        }


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

    private void switchBookmarkIcon() {
        List< Integer > list = Kernel.getLocalProvider().getBookmarkInfo( _ids[ 0 ] );
        TreeSet< Integer > set = new TreeSet< Integer >( list );
        int currentPage = ( ( PageSettable ) _view).getCurrentPage();
        if ( set.contains( currentPage ) ) {
            set.remove( currentPage  );
            ( ( ImageView ) _bookmarkView).setImageBitmap( LoadBitmap( R.drawable.button_bookmark ) );
        } else {
            set.add( currentPage );
            ( ( ImageView ) _bookmarkView).setImageBitmap( LoadBitmap( R.drawable.button_bookmark_on ) );
        }
        Kernel.getLocalProvider().setBookmarkInfo( _ids[ 0 ] , new ArrayList< Integer >( set ) );
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
