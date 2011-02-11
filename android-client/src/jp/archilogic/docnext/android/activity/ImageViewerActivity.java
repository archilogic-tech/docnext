package jp.archilogic.docnext.android.activity;

import java.util.List;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.component.CustomZoomControls;
import jp.archilogic.docnext.android.task.BitmapReceiver;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.GetPageTask;
import jp.archilogic.docnext.android.task.IntegerReceiver;
import jp.archilogic.docnext.android.widget.CoreImageView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageViewerActivity extends Activity {
    private CoreImageView _coreImageView;
    private ImageView _leftLoadingImageView;
    private ImageView _rightLoadingImageView;
    private TextView _currentPageTextView;
    private TextView _totalPageTextView;
    private CustomZoomControls _zoomControls;

    private long _id;
    private int _currentPage;
    private int _totalPage;
    private Bitmap[] _bitmapCache;
    private boolean[] _loaded;

    private void changePage( final int page , final boolean leftToRight ) {
        if ( page < 0 || page >= _totalPage || !_loaded[ page - _currentPage + 1 ] ) {
            return;
        }

        if ( page > _currentPage ) {
            updateState( 0 , 1 , 2 );
        } else {
            updateState( 2 , 1 , 0 );
        }

        if ( leftToRight ) {
            // _switcher.setLeftToRightAnimation();
            _rightLoadingImageView.setVisibility( View.INVISIBLE );
        } else {
            // _switcher.setRightToLeftAnimatin();
            _leftLoadingImageView.setVisibility( View.INVISIBLE );
        }

        final int nextNextPage = page + page - _currentPage;
        setCurrentPage( page );

        // _switcher.setCurrentBitmap( _bitmapCache[ 1 ] );
        // _switcher.initScroll( false );
        loadImage( nextNextPage );
    }

    private void initComonentVariable() {
        _coreImageView = ( CoreImageView ) findViewById( R.id.coreImageView );
        _leftLoadingImageView = ( ImageView ) findViewById( R.id.LeftLoadingImageView );
        _rightLoadingImageView = ( ImageView ) findViewById( R.id.RightLoadingImageView );
        _currentPageTextView = ( TextView ) findViewById( R.id.CurrentPageTextView );
        _totalPageTextView = ( TextView ) findViewById( R.id.TotalPageTextView );
        _zoomControls = ( CustomZoomControls ) findViewById( R.id.ZoomControls01 );
    }

    private void loadImage( final int page ) {
        if ( page < 0 || page >= _totalPage ) {
            return;
        }

        if ( page == _currentPage - 1 ) {
            _rightLoadingImageView.setVisibility( View.VISIBLE );
        } else if ( page == _currentPage + 1 ) {
            _leftLoadingImageView.setVisibility( View.VISIBLE );
        }

        new GetPageTask( _id , page , 0 , 0 , 0 , new BitmapReceiver() {
            @Override
            public void receiver( final Bitmap result ) {
                final int index = page - _currentPage + 1;

                if ( index < 0 || index >= _bitmapCache.length ) {
                    return;
                }

                if ( index == 0 ) {
                    _rightLoadingImageView.setVisibility( View.INVISIBLE );
                } else if ( index == 2 ) {
                    _leftLoadingImageView.setVisibility( View.INVISIBLE );
                }

                _bitmapCache[ index ] = result;
                _loaded[ index ] = true;

                // for init (mostly
                if ( index == 1 ) {
                    // _switcher.setCurrentBitmap( result );
                }
            }
        } ).execute();
    }

    public void moveLeft() {
        changePage( _currentPage + 1 , true );
    }

    public void moveRight() {
        changePage( _currentPage - 1 , false );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.image_viewer );

        final boolean test = false;
        if ( test ) {
            final String action = getIntent().getData().getHost();

            if ( !action.equals( "view" ) ) {
                Toast.makeText( this , "Unsupported Action" , Toast.LENGTH_LONG ).show();
                finish();
                return;
            }

            final List< String > segments = getIntent().getData().getPathSegments();

            if ( segments.size() != 1 ) {
                Toast.makeText( this , "Invalid paramaters" , Toast.LENGTH_LONG ).show();
                finish();
                return;
            }

            _id = Long.valueOf( segments.get( 0 ) );
        } else {
            _id = 66;
        }

        initComonentVariable();

        // _switcher.init( this );
        // _zoomControls.init( _switcher );

        _bitmapCache = new Bitmap[ 3 ];
        _loaded = new boolean[ 3 ];

        new DownloadTask( _id , new IntegerReceiver() {
            @Override
            public void receiver( final Integer result ) {
                setTotalPage( result );
                setCurrentPage( 0 );

                _bitmapCache = new Bitmap[ 3 ];
                _loaded = new boolean[ 3 ];
                loadImage( 0 );
                loadImage( 1 );
            }
        } );// .execute();
    }

    @Override
    public void onWindowFocusChanged( final boolean hasFocus ) {
        super.onWindowFocusChanged( hasFocus );

        if ( hasFocus ) {
            ( ( AnimationDrawable ) _leftLoadingImageView.getBackground() ).start();
            ( ( AnimationDrawable ) _rightLoadingImageView.getBackground() ).start();
        }
    }

    private void setCurrentPage( final int currentPage ) {
        _currentPage = currentPage;
        // change to 1-origin
        _currentPageTextView.setText( String.valueOf( currentPage + 1 ) );
    }

    private void setTotalPage( final int totalPage ) {
        _totalPage = totalPage;
        _totalPageTextView.setText( String.valueOf( totalPage ) );
    }

    private void updateState( final int i0 , final int i1 , final int i2 ) {
        _bitmapCache[ i0 ] = _bitmapCache[ i1 ];
        _bitmapCache[ i1 ] = _bitmapCache[ i2 ];
        _loaded[ i0 ] = _loaded[ i1 ];
        _loaded[ i1 ] = _loaded[ i2 ];
        _loaded[ i2 ] = false;
    }
}
