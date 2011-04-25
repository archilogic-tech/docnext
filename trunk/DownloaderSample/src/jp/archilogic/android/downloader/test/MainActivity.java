package jp.archilogic.android.downloader.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.archilogic.android.downloader.DownloadInfo;
import jp.archilogic.android.downloader.DownloadOption;
import jp.archilogic.android.downloader.Downloader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.common.collect.Lists;

public class MainActivity extends Activity {
    private static final String KEY_ID = "key_id";

    private TextView _message;

    private String _remote;
    private final String _local = "/sdcard/downloader/dummy";

    private final BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            final String action = intent.getAction();

            if ( action.equals( Downloader.ACTION_DOWNLOADED ) ) {
                message( "Downloaded" );
                message( "ID: " + intent.getBundleExtra( Downloader.EXTRA_EXTRA ).getInt( KEY_ID ) );
            } else if ( action.equals( Downloader.ACTION_COMPLETED ) ) {
                message( "Completed" );
            } else if ( action.equals( Downloader.ACTION_STOPPED ) ) {
                message( "Stopped" );
                message( "ID: " + intent.getBundleExtra( Downloader.EXTRA_EXTRA ).getInt( KEY_ID ) );
                message( "Error: " + intent.getSerializableExtra( Downloader.EXTRA_ERROR ) );
            } else if ( action.equals( Downloader.ACTION_RESUMED ) ) {
                message( "Resumed" );
            } else if ( action.equals( Downloader.ACTION_ERROR ) ) {
                message( "Error" );
                message( "ID: " + intent.getBundleExtra( Downloader.EXTRA_EXTRA ).getInt( KEY_ID ) );
                message( "Error: " + intent.getSerializableExtra( Downloader.EXTRA_ERROR ) );
            }
        }
    };

    private void message( final String message ) {
        _message.setText( message + "\n" + _message.getText() );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        prepare();

        _message = ( TextView ) findViewById( R.id.debug );

        findViewById( R.id.button1 ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                final List< DownloadInfo > infos = Lists.newArrayList();

                for ( int index = 0 ; index < 3 ; index++ ) {
                    final Bundle extra = new Bundle();
                    extra.putInt( KEY_ID , index );

                    infos.add( new DownloadInfo( _remote + index , _local + index , extra ) );
                }

                Downloader.start( MainActivity.this , infos , new DownloadOption.Builder().canResume( true ).build() );
            }
        } );

        findViewById( R.id.button2 ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                for ( int index = 0 ; index < 3 ; index++ ) {
                    FileUtils.deleteQuietly( new File( _local + index ) );
                }
            }
        } );
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver( _receiver );
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter filter = new IntentFilter();
        filter.addAction( Downloader.ACTION_DOWNLOADED );
        filter.addAction( Downloader.ACTION_COMPLETED );
        filter.addAction( Downloader.ACTION_STOPPED );
        filter.addAction( Downloader.ACTION_RESUMED );
        filter.addAction( Downloader.ACTION_ERROR );

        registerReceiver( _receiver , filter );
    }

    private void prepare() {
        try {
            final InputStream in = getAssets().open( "remote_path.txt" );
            _remote = IOUtils.toString( in ) + "dummy";
            IOUtils.closeQuietly( in );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
