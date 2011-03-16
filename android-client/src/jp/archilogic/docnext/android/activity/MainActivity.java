package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.provider.remote.RemoteProvider;
import jp.archilogic.docnext.android.type.TaskErrorType;
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
            if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_SUCCEED ) ) {
                final long id = intent.getLongExtra( RemoteProvider.EXTRA_ID , -1 );

                checkDockInfo( Kernel.getLocalProvider().getDocInfo( id ) );
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_SUCCEED ) ) {

            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED ) ) {
                final long id = intent.getLongExtra( RemoteProvider.EXTRA_ID , -1 );
                final int page = intent.getIntExtra( RemoteProvider.EXTRA_PAGE , -1 );
                // final int level = intent.getIntExtra( RemoteProvider.EXTRA_LEVEL , -1 );
                // final int px = intent.getIntExtra( RemoteProvider.EXTRA_PX , -1 );
                // final int py = intent.getIntExtra( RemoteProvider.EXTRA_PY , -1 );

                final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

                if ( page == 1 ) {
                    startCoreView( doc );
                }

                ensureThumbnail( doc , page + 1 );
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED ) ) {

            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_THUMBNAIL_SUCCEED ) ) {
                final long id = intent.getLongExtra( RemoteProvider.EXTRA_ID , -1 );
                final int page = intent.getIntExtra( RemoteProvider.EXTRA_PAGE , -1 );

                ensureImage( Kernel.getLocalProvider().getDocInfo( id ) , page );
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_THUMBNAIL_FAILED ) ) {
                final TaskErrorType error = ( TaskErrorType ) intent.getSerializableExtra( RemoteProvider.EXTRA_ERROR );

                switch ( error ) {
                case NETWORK_UNAVAILABLE:
                    Toast.makeText( _self , R.string.network_unavailable , Toast.LENGTH_LONG ).show();
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

        filter.addAction( RemoteProvider.BROADCAST_GET_DOC_INFO_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_DOC_INFO_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_FONT_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_FONT_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_IMAGE_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_THUMBNAIL_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_THUMBNAIL_FAILED );

        return filter;
    }

    private void checkDockInfo( final DocInfo doc ) {
        switch ( doc.type ) {
        case IMAGE:
            if ( Kernel.getLocalProvider().isCompleted( doc.id ) ) {
                startCoreView( doc );
            } else {
                if ( Kernel.getRemoteProvider().isWorking() ) {
                    Toast.makeText( _self , R.string.cannot_download_in_parallel , Toast.LENGTH_LONG ).show();
                } else {
                    Kernel.getRemoteProvider().setWorking( true );

                    ensureThumbnail( doc , 0 );
                }
            }
            break;
        case TEXT:
            // fetchText( id , meta );
            break;
        default:
            throw new RuntimeException();
        }
    }

    private void ensureImage( final DocInfo doc , final int page ) {
        if ( Kernel.getLocalProvider().getImagePath( doc.id , page , 0 , 0 , 0 ) != null ) {
            sendBroadcast( new Intent( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED ). //
                    putExtra( RemoteProvider.EXTRA_ID , doc.id ). //
                    putExtra( RemoteProvider.EXTRA_PAGE , page ). //
                    putExtra( RemoteProvider.EXTRA_LEVEL , 0 ). //
                    putExtra( RemoteProvider.EXTRA_PX , 0 ). //
                    putExtra( RemoteProvider.EXTRA_PY , 0 ) );
        } else {
            Kernel.getRemoteProvider().getImage( _self , doc.id , page , 0 , 0 , 0 );
        }
    }

    private void ensureThumbnail( final DocInfo doc , final int page ) {
        if ( page < doc.pages ) {
            setProgress( Window.PROGRESS_END * page / doc.pages );

            if ( Kernel.getLocalProvider().getThumbnailPath( doc.id , page ) != null ) {
                sendBroadcast( new Intent( RemoteProvider.BROADCAST_GET_THUMBNAIL_SUCCEED ). //
                        putExtra( RemoteProvider.EXTRA_ID , doc.id ). //
                        putExtra( RemoteProvider.EXTRA_PAGE , page ) );
            } else {
                Kernel.getRemoteProvider().getThumbnail( _self , doc.id , page );
            }
        } else {
            setProgressBarVisibility( false );

            Kernel.getLocalProvider().setCompleted( doc.id );
            Kernel.getRemoteProvider().setWorking( false );
        }
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        setContentView( R.layout.main );

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        findViewById( R.id.button ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                final long id = 0;

                final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

                if ( doc != null ) {
                    checkDockInfo( doc );
                } else {
                    Kernel.getRemoteProvider().getDocInfo( _self , id );
                }
            }
        } );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
    }

    private void startCoreView( final DocInfo doc ) {
        startActivity( new Intent( _self , CoreViewActivity.class ).putExtra( CoreViewActivity.EXTRA_IDS ,
                new long[] { doc.id } ) );
    }
}
