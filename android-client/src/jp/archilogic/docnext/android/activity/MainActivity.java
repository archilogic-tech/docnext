package jp.archilogic.docnext.android.activity;

import java.io.File;
import java.io.IOException;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.service.DownloadService;
import jp.archilogic.docnext.android.type.TaskErrorType;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final MainActivity _self = this;

    private final BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            if ( intent.getAction().equals( DownloadService.BROADCAST_DOWNLOAD_PROGRESS ) ) {
                final int current = intent.getIntExtra( DownloadService.EXTRA_CURRENT , -1 );
                final int total = intent.getIntExtra( DownloadService.EXTRA_TOTAL , -1 );
                final int imagePerPage =
                        intent.getIntExtra( DownloadService.EXTRA_ITEM_PER_PAGE , -1 );

                if ( current < total ) {
                    setProgress( Window.PROGRESS_END * current / total );

                    if ( current == 2 * imagePerPage ) {
                        startCoreView( Kernel.getLocalProvider().getDocInfo(
                                Kernel.getAppStateManager().getDownloadTarget() ) );
                    }
                } else {
                    setProgressBarVisibility( false );
                }
            } else if ( intent.getAction().equals( DownloadService.BROADCAST_DOWNLOAD_FAILED ) ) {
                final TaskErrorType error =
                        ( TaskErrorType ) intent.getSerializableExtra( DownloadService.EXTRA_ERROR );

                switch ( error ) {
                case NETWORK_UNAVAILABLE:
                    Toast.makeText( _self , R.string.network_unavailable , Toast.LENGTH_LONG )
                            .show();
                    break;
                case NETWORK_ERROR:
                    Toast.makeText( _self , R.string.network_error , Toast.LENGTH_LONG ).show();
                    break;
                default:
                    throw new RuntimeException();
                }
            }
        }
    };

    public IntentFilter buildRemoteProviderReceiverFilter() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_PROGRESS );
        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_FAILED );

        return filter;
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        setContentView( R.layout.main );

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        findViewById( R.id.button0 ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                requestDocument( 2 );
            }
        } );
        findViewById( R.id.button1 ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                requestDocument( 10 );
            }
        } );
        findViewById( R.id.button2 ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                requestDocument( 12 );
            }
        } );
        findViewById( R.id.reset ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                try {
                    FileUtils.deleteDirectory( new File( "/sdcard/docnext" ) );
                } catch ( final IOException e ) {
                    throw new RuntimeException( e );
                }
            }
        } );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
    }

    private void requestDocument( final long id ) {
        final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

        if ( doc != null ) {
            if ( Kernel.getLocalProvider().isCompleted( doc.id )
                    || Kernel.getAppStateManager().getDownloadTarget() == id
                    && Kernel.getLocalProvider().isImageExists( doc.id , 1 ) ) {
                startCoreView( doc );
            } else {
                Toast.makeText( _self , R.string.cannot_download_in_parallel , Toast.LENGTH_LONG )
                        .show();
            }
        } else {
            if ( Kernel.getAppStateManager().getDownloadTarget() == -1 ) {
                Kernel.getAppStateManager().setDownloadTarget( id );

                startService( new Intent( _self , DownloadService.class ) );
            } else {
                Toast.makeText( _self , R.string.cannot_download_in_parallel , Toast.LENGTH_LONG )
                        .show();
            }
        }
    }

    private void startCoreView( final DocInfo doc ) {
        startActivity( new Intent( _self , CoreViewActivity.class ).putExtra(
                CoreViewActivity.EXTRA_IDS , new long[] { doc.id } ) );
    }
}
