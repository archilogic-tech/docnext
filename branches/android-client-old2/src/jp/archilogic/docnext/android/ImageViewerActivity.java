package jp.archilogic.docnext.android;

import java.util.List;

import jp.archilogic.docnext.android.core.OnPageChangedListener;
import jp.archilogic.docnext.android.core.image.CoreImageView;
import jp.archilogic.docnext.android.core.image.ImageDocDirection;
import jp.archilogic.docnext.android.info.MetaInfo;
import jp.archilogic.docnext.android.type.ExtraType;
import jp.archilogic.docnext.android.util.StorageUtil;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.common.collect.Lists;

public class ImageViewerActivity extends Activity {
    private CoreImageView _coreImageView;
    private TextView _currentPageTextView;
    private TextView _totalPageTextView;

    private final OnPageChangedListener _coreImageListener = new OnPageChangedListener() {
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

    private void assignWidget() {
        _coreImageView = ( CoreImageView ) findViewById( R.id.coreImageView );
        _currentPageTextView = ( TextView ) findViewById( R.id.CurrentPageTextView );
        _totalPageTextView = ( TextView ) findViewById( R.id.TotalPageTextView );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.image_viewer );

        final long id = getIntent().getLongExtra( ExtraType.ID.toString() , -1 );
        if ( id == -1 ) {
            throw new RuntimeException();
        }

        assignWidget();

        final MetaInfo meta = StorageUtil.getMetaInfo( id );

        final List< String > sources = Lists.newArrayList();
        for ( int index = 0 ; index < meta.pages ; index++ ) {
            sources.add( StorageUtil.getImagePath( id , index ) );
        }
        _coreImageView.setSources( sources );

        final List< String > thumbs = Lists.newArrayList();
        for ( int index = 0 ; index < meta.pages ; index++ ) {
            thumbs.add( StorageUtil.getImageThumbnailPath( id , index ) );
        }
        _coreImageView.setThumbnailSources( thumbs );

        _coreImageView.setDirection( ImageDocDirection.R2L );
        _coreImageView.setListener( _coreImageListener );

        _currentPageTextView.setText( String.valueOf( 1 ) );
        _totalPageTextView.setText( String.valueOf( sources.size() ) );
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu menu ) {
        menu.add( Menu.NONE , ImageDocDirection.L2R.ordinal() , Menu.NONE , "L2R" );
        menu.add( Menu.NONE , ImageDocDirection.R2L.ordinal() , Menu.NONE , "R2L" );
        menu.add( Menu.NONE , ImageDocDirection.T2B.ordinal() , Menu.NONE , "T2B" );
        menu.add( Menu.NONE , ImageDocDirection.B2T.ordinal() , Menu.NONE , "B2T" );

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( final MenuItem item ) {
        _coreImageView.setDirection( ImageDocDirection.values()[ item.getItemId() ] );

        return true;
    }
}
