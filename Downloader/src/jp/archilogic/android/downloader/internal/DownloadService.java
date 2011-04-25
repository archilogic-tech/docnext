package jp.archilogic.android.downloader.internal;

import java.util.List;

import jp.archilogic.android.downloader.DownloadError;
import jp.archilogic.android.downloader.DownloadInfo;
import jp.archilogic.android.downloader.DownloadOption;
import jp.archilogic.android.downloader.Downloader;
import jp.archilogic.android.downloader.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.google.common.collect.Lists;

public class DownloadService extends Service {
    private static final String PREFIX = DownloadService.class.getName();

    public static final String EXTRA_INFOS = PREFIX + ".extra.infos";
    public static final String EXTRA_OPTION = PREFIX + ".extra.option";

    private static final String ACTION_RESUME = PREFIX + ".action.resume";
    private static final String EXTRA_INDEX = PREFIX + ".extra.index";

    // need to be dynamic? (or Collection?)
    private static final int NOTIFICATION_ID = 1234;
    private static final int NOTIFY_DURATION = 1000;

    private NotificationManager _manager;

    private List< DownloadInfo > _infos = null;
    private DownloadOption _option;

    private List< Long > _contentLengths;
    private Notification _notification;
    private long _latestNotifyTime;
    private int _index;
    private long _progress;

    private final BroadcastReceiver _resumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            unregisterReceiver( _resumeReceiver );

            _notification.icon = R.anim.loading;
            _notification.contentIntent = PendingIntent.getActivity( DownloadService.this , 0 , new Intent() , 0 );
            _notification.contentView.setTextViewText( R.id.message , _option.getOnGoingMessage() );
            _manager.notify( NOTIFICATION_ID , _notification );

            sendBroadcast( new Intent( Downloader.ACTION_RESUMED ) );

            download( intent.getIntExtra( EXTRA_INDEX , -1 ) , true );
        }
    };

    private float calcProgress( final int index , final long progress ) {
        long already = 0;
        long current = 0;

        for ( int index_ = 0 ; index_ <= index ; index_++ ) {
            if ( index_ < index ) {
                already += _contentLengths.get( index_ );
            } else {
                current += _contentLengths.get( index_ );
            }
        }

        final float total = 1f * ( already + current ) * _infos.size() / ( index + 1 );

        return ( already + progress ) / total;
    }

    private void download( final int index , final boolean tryResume ) {
        if ( index < _infos.size() ) {
            _index = index;

            final DownloadInfo info = _infos.get( index );

            new DownloadTask( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void contentLength( final long contentLength ) {
                    _contentLengths.add( contentLength );
                }

                @Override
                public void error( final DownloadError error ) {
                    handleError( index , error );
                }

                @Override
                public void progress( final long progress ) {
                    updateNotification( index , progress , false );
                }

                @Override
                public void receive( final Void result ) {
                    sendBroadcast( new Intent( Downloader.ACTION_DOWNLOADED ). //
                            putExtra( Downloader.EXTRA_EXTRA , _infos.get( index ).getExtra() ) );

                    updateNotification( index , 1 , false );
                    download( index + 1 , false );
                }
            } , info.getRemotePath() , info.getLocalPath() , tryResume ).execute();
        } else {
            sendBroadcast( new Intent( Downloader.ACTION_COMPLETED ) );

            finish();
        }
    }

    private void finish() {
        hideNotification();
        stopSelf();
    }

    private void handleError( final int index , final DownloadError error ) {
        if ( _option.canResume() ) {
            sendBroadcast( new Intent( Downloader.ACTION_STOPPED ). //
                    putExtra( Downloader.EXTRA_EXTRA , _infos.get( index ).getExtra() ). //
                    putExtra( Downloader.EXTRA_ERROR , error ) );

            registerReceiver( _resumeReceiver , new IntentFilter( ACTION_RESUME ) );

            _notification.icon = R.drawable.suspend;
            _notification.contentIntent =
                    PendingIntent.getBroadcast( this , 0 , new Intent( ACTION_RESUME ).putExtra( EXTRA_INDEX , index ) , 0 );
            _notification.contentView.setTextViewText( R.id.message , _option.getSuspendMessage() );
            _manager.notify( NOTIFICATION_ID , _notification );
        } else {
            sendBroadcast( new Intent( Downloader.ACTION_ERROR ). //
                    putExtra( Downloader.EXTRA_EXTRA , _infos.get( index ).getExtra() ). //
                    putExtra( Downloader.EXTRA_ERROR , error ) );

            if ( _option.abortOnError() ) {
                finish();
            } else {
                updateNotification( index , 1 , false );
                download( index + 1 , false );
            }
        }
    }

    private void hideNotification() {
        final NotificationManager manager = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );

        manager.cancel( NOTIFICATION_ID );
    }

    @Override
    public IBinder onBind( final Intent intent ) {
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void onStart( final Intent intent , final int startId ) {
        super.onStart( intent , startId );

        System.err.println( "onStart" );

        // TODO consider to be started in parallel

        if ( _infos == null ) {
            _manager = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );

            _infos = ( List< DownloadInfo > ) intent.getSerializableExtra( EXTRA_INFOS );
            _option = ( DownloadOption ) intent.getSerializableExtra( EXTRA_OPTION );

            _contentLengths = Lists.newArrayList();

            showNotification();

            download( 0 , false );
        } else {
            _infos.addAll( ( List< DownloadInfo > ) intent.getSerializableExtra( EXTRA_INFOS ) );
            updateNotification( _index , _progress , true );
        }
    }

    private void showNotification() {
        _notification = new Notification( R.anim.loading , _option.getTicker() , System.currentTimeMillis() );
        _notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;
        _notification.contentIntent = PendingIntent.getActivity( this , 0 , new Intent() , 0 );

        final RemoteViews view = new RemoteViews( getPackageName() , R.layout.notification );

        if ( Build.VERSION.SDK_INT <= 8 ) {
            view.setTextColor( R.id.message , Color.BLACK );
            view.setTextColor( R.id.rate , Color.BLACK );
            view.setTextColor( R.id.number , Color.BLACK );
        }

        view.setTextViewText( R.id.message , _option.getOnGoingMessage() );
        view.setTextViewText( R.id.rate , "0%" );
        view.setTextViewText( R.id.number , "(0/0)" );

        _notification.contentView = view;

        _manager.notify( NOTIFICATION_ID , _notification );

        _latestNotifyTime = SystemClock.elapsedRealtime();
    }

    private void updateNotification( final int index , final long progress , final boolean forceUpdate ) {
        final int MAX = 10000;

        _progress = progress;

        final long time = SystemClock.elapsedRealtime();

        if ( forceUpdate || time - _latestNotifyTime > NOTIFY_DURATION ) {
            final float rate = calcProgress( index , progress );

            _notification.contentView.setProgressBar( R.id.progress , MAX , Math.round( MAX * rate ) , false );
            _notification.contentView.setTextViewText( R.id.rate , String.format( "%d%%" , Math.round( 100 * rate ) ) );
            _notification.contentView.setTextViewText( R.id.number , String.format( "(%d/%d)" , index + 1 , _infos.size() ) );

            _manager.notify( NOTIFICATION_ID , _notification );

            _latestNotifyTime = time;
        }
    }
}
