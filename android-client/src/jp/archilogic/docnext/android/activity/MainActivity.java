package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
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
                final int level = intent.getIntExtra( RemoteProvider.EXTRA_LEVEL , -1 );
                final int px = intent.getIntExtra( RemoteProvider.EXTRA_PX , -1 );
                final int py = intent.getIntExtra( RemoteProvider.EXTRA_PY , -1 );

                final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );
                final ImageInfo image = Kernel.getLocalProvider().getImageInfo( id );

                if ( page == 2 && level == 0 && px == 0 && py == 0 ) {
                    startCoreView( doc );
                }

                if ( py + 1 < image.ny ) {
                    ensureImage( doc , page , level , px , py + 1 );
                } else if ( px + 1 < image.nx ) {
                    ensureImage( doc , page , level , px + 1 , 0 );
                } else if ( level + 1 < 1 ) {
                    ensureImage( doc , page , level + 1 , 0 , 0 );
                } else if ( page + 1 < doc.pages ) {
                    setProgress( Window.PROGRESS_END * ( page + 1 ) / doc.pages );

                    ensureImage( doc , page + 1 , 0 , 0 , 0 );
                } else {
                    setProgressBarVisibility( false );

                    Kernel.getLocalProvider().setCompleted( doc.id );
                    Kernel.getRemoteProvider().setWorking( false );
                }
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_INFO_SUCCEED ) ) {
                final long id = intent.getLongExtra( RemoteProvider.EXTRA_ID , -1 );

                ensureImage( Kernel.getLocalProvider().getDocInfo( id ) , 0 , 0 , 0 , 0 );
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED ) ) {
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
        filter.addAction( RemoteProvider.BROADCAST_GET_IMAGE_INFO_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_IMAGE_INFO_FAILED );
        filter.addAction( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED );
        filter.addAction( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED );

        return filter;
    }

    private void checkDockInfo( final DocInfo doc ) {
        switch ( doc.type ) {
        case IMAGE:
            if ( Kernel.getRemoteProvider().isWorking() ) {
                Toast.makeText( _self , R.string.cannot_download_in_parallel , Toast.LENGTH_LONG ).show();
            } else {
                Kernel.getRemoteProvider().setWorking( true );

                ensureImageInfo( doc );
            }
            break;
        case TEXT:
            break;
        default:
            throw new RuntimeException();
        }
    }

    private void ensureImage( final DocInfo doc , final int page , final int level , final int px , final int py ) {
        if ( Kernel.getLocalProvider().getImagePath( doc.id , page , level , px , py ) != null ) {
            sendBroadcast( new Intent( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED ). //
                    putExtra( RemoteProvider.EXTRA_ID , doc.id ). //
                    putExtra( RemoteProvider.EXTRA_PAGE , page ). //
                    putExtra( RemoteProvider.EXTRA_LEVEL , level ). //
                    putExtra( RemoteProvider.EXTRA_PX , px ). //
                    putExtra( RemoteProvider.EXTRA_PY , py ) );
        } else {
            Kernel.getRemoteProvider().getImage( _self , doc.id , page , level , px , py );
        }
    }

    private void ensureImageInfo( final DocInfo doc ) {
        if ( Kernel.getLocalProvider().getImageInfo( doc.id ) != null ) {
            sendBroadcast( new Intent( RemoteProvider.BROADCAST_GET_IMAGE_INFO_SUCCEED ). //
                    putExtra( RemoteProvider.EXTRA_ID , doc.id ) );
        } else {
            Kernel.getRemoteProvider().getImageInfo( _self , doc.id );
        }
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        setContentView( R.layout.main );

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        // TODO downloading on Activity has a problem on quit App (should use Service?)
        findViewById( R.id.button ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                final long id = 0;

                final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

                if ( doc != null ) {
                    if ( Kernel.getLocalProvider().isCompleted( doc.id ) ) {
                        startCoreView( doc );
                    } else {
                        sendBroadcast( new Intent( RemoteProvider.BROADCAST_GET_DOC_INFO_SUCCEED ). //
                                putExtra( RemoteProvider.EXTRA_ID , doc.id ) );
                    }
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
