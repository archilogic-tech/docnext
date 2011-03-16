package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.CoreViewType;
import jp.archilogic.docnext.android.provider.remote.RemoteProvider;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

public class CoreViewActivity extends Activity implements CoreViewDelegate {
    public static final String EXTRA_IDS = "jp.archilogic.docnext.android.activity.CoreViewActivity.ids";

    private ViewGroup _rootViewGroup;

    private final BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_SUCCEED ) ) {
                final long id = intent.getLongExtra( RemoteProvider.EXTRA_ID , -1 );
                final int page = intent.getIntExtra( RemoteProvider.EXTRA_PAGE , -1 );
                // final int level = intent.getIntExtra( RemoteProvider.EXTRA_LEVEL , -1 );
                // final int px = intent.getIntExtra( RemoteProvider.EXTRA_PX , -1 );
                // final int py = intent.getIntExtra( RemoteProvider.EXTRA_PY , -1 );

                final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

                if ( page + 1 < doc.pages ) {
                    setProgress( Window.PROGRESS_END * ( page + 1 ) / doc.pages );
                } else {
                    setProgressBarVisibility( false );
                }
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_THUMBNAIL_SUCCEED ) ) {
            } else if ( intent.getAction().equals( RemoteProvider.BROADCAST_GET_DOC_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_FONT_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_IMAGE_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_TEXT_INFO_FAILED )
                    || intent.getAction().equals( RemoteProvider.BROADCAST_GET_THUMBNAIL_FAILED ) ) {
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

    @Override
    public void changeCoreViewType( final CoreViewType type ) {
        // Not implemented
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );

        _rootViewGroup = new FrameLayout( this );

        setContentView( _rootViewGroup );

        registerReceiver( _remoteProviderReceiver , buildRemoteProviderReceiverFilter() );

        final long[] ids = getIntent().getLongArrayExtra( EXTRA_IDS );
        if ( ids == null || ids.length == 0 ) {
            throw new RuntimeException();
        }

        final CoreView view = validateCoreViewType( ids ).buildView( this );

        _rootViewGroup.addView( ( View ) view );

        view.setIds( ids );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver( _remoteProviderReceiver );
    }

    private CoreViewType validateCoreViewType( final long[] ids ) {
        CoreViewType ret = null;

        for ( final long id : ids ) {
            final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );

            if ( ret == null ) {
                ret = doc.type;
            } else if ( ret != doc.type ) {
                throw new RuntimeException();
            }
        }

        return ret;
    }
}
