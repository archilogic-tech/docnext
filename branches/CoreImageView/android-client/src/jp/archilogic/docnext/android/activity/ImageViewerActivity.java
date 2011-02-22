package jp.archilogic.docnext.android.activity;

import java.util.List;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.core.image.CoreImageListener;
import jp.archilogic.docnext.android.core.image.CoreImageView;
import jp.archilogic.docnext.android.core.image.ImageDocDirection;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.google.common.collect.Lists;

public class ImageViewerActivity extends Activity {
    private CoreImageView _coreImageView;
    private TextView _currentPageTextView;
    private TextView _totalPageTextView;

    private final CoreImageListener _coreImageListener = new CoreImageListener() {
        @Override
        public void onPageChanged( final int index ) {
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    _currentPageTextView.setText( String.valueOf( index + 1 ) );
                }
            } );
        }
    };

    private void initComonentVariable() {
        _coreImageView = ( CoreImageView ) findViewById( R.id.coreImageView );
        _currentPageTextView = ( TextView ) findViewById( R.id.CurrentPageTextView );
        _totalPageTextView = ( TextView ) findViewById( R.id.TotalPageTextView );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_NO_TITLE );

        setContentView( R.layout.image_viewer );

        initComonentVariable();

        final List< String > sources = Lists.newArrayList();
        for ( int index = 0 ; index < 180 ; index++ ) {
            sources.add( String.format( "/sdcard/docnext/hanako-%03d.jpg" , index + 1 ) );
        }
        _coreImageView.setSources( sources );

        final List< String > thumbs = Lists.newArrayList();
        for ( int index = 0 ; index < 180 ; index++ ) {
            thumbs.add( String.format( "/sdcard/docnext/hanako-thumb-%03d.jpg" , index + 1 ) );
        }
        _coreImageView.setThumbnailSources( thumbs );

        _coreImageView.setDirection( ImageDocDirection.R2L );
        _coreImageView.setListener( _coreImageListener );

        findViewById( R.id.l2r ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _coreImageView.setDirection( ImageDocDirection.L2R );
            }
        } );
        findViewById( R.id.r2l ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _coreImageView.setDirection( ImageDocDirection.R2L );
            }
        } );
        findViewById( R.id.t2b ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _coreImageView.setDirection( ImageDocDirection.T2B );
            }
        } );
        findViewById( R.id.b2t ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _coreImageView.setDirection( ImageDocDirection.B2T );
            }
        } );

        _currentPageTextView.setText( String.valueOf( 1 ) );
        _totalPageTextView.setText( String.valueOf( sources.size() ) );
    }
}
