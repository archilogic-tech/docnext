package jp.archilogic.docnext.android.widget;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.service.DownloadService;
import android.R.attr;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class CoreViewStatusBar extends LinearLayout {

    private ProgressBar _bar;

    private final BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive( final Context context , final Intent intent ) {
            
            if ( intent.getAction().equals( DownloadService.BROADCAST_DOWNLOAD_PROGRESS ) ) {
                final int current = intent.getIntExtra( DownloadService.EXTRA_CURRENT , -1 );
                final int total = intent.getIntExtra( DownloadService.EXTRA_TOTAL , -1 );

                _bar.setMax( total );
                if ( current < total ) {
                    _bar.setProgress( current );
                } else {
                    _bar.setVisibility( INVISIBLE );
                    getContext().unregisterReceiver( _remoteProviderReceiver );
                }
            }
        }

    };

    public CoreViewStatusBar( Context context , long id ) {
        super( context );
    
        setLayoutParams( new FrameLayout.LayoutParams( FrameLayout.LayoutParams.FILL_PARENT ,
                FrameLayout.LayoutParams.WRAP_CONTENT , Gravity.BOTTOM ) );
        setOrientation( LinearLayout.VERTICAL );
        setBackgroundColor( 0x80000000 );
        setPadding( dp( 5 ) , dp( 10 ) , dp( 5 ) , dp( 10 ) );
        setVisibility( GONE );

        buildProgressBar();
    }

    private void buildProgressBar() {
        int style = attr.progressBarStyleHorizontal;
        _bar = new ProgressBar( getContext() , null , style );
        
        _bar.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT ,
                LinearLayout.LayoutParams.WRAP_CONTENT , Gravity.BOTTOM ) );
        _bar.setProgressDrawable( getResources().getDrawable( R.drawable.progress_horizontal ) );
        addView( _bar );
        
        IntentFilter filter = new IntentFilter();
        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_PROGRESS );
        getContext().registerReceiver( _remoteProviderReceiver , filter);
        
    }

    private int dp( float value ) {
        final float density = getResources().getDisplayMetrics().density;

        return Math.round( value * density );
    }
}
