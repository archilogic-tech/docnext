package jp.archilogic.docnext.android.service;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.task.Receiver;
import jp.archilogic.docnext.android.type.TaskErrorType;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class DownloadService extends Service {
    private class DownloadReceiver implements Receiver< Void > {
        @Override
        public void error( final TaskErrorType error ) {
            getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_FAILED ). //
                    putExtra( EXTRA_ERROR , error ) );
        }

        @Override
        public void receive( final Void result ) {
        }
    }

    private static final String PREFIX = DownloadService.class.getName();

    public static final String BROADCAST_DOWNLOAD_PROGRESS = PREFIX + ".download.progress";
    public static final String BROADCAST_DOWNLOAD_FAILED = PREFIX + ".download.failed";

    public static final String EXTRA_PAGE = PREFIX + ".extra.page";
    public static final String EXTRA_PAGES = PREFIX + ".extra.pages";
    public static final String EXTRA_ERROR = PREFIX + ".extra.error";

    private long _id;
    private DocInfo _doc;

    private void checkDockInfo() {
        _doc = Kernel.getLocalProvider().getDocInfo( _id );

        switch ( _doc.type ) {
        case IMAGE:
            ensureImageInfo();
            break;
        case TEXT:
            break;
        default:
            throw new RuntimeException();
        }
    }

    private void ensureDocInfo() {
        if ( Kernel.getLocalProvider().getDocInfo( _id ) == null ) {
            Kernel.getRemoteProvider().getDocInfo( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    checkDockInfo();
                }
            } , _id ).execute();
        } else {
            checkDockInfo();
        }
    }

    private void ensureImage( final ImageInfo image , final int nLevel , final int page , final int level ,
            final int px , final int py ) {
        if ( page < _doc.pages ) {
            if ( level < nLevel ) {
                final int factor = ( int ) Math.pow( 2 , level );

                if ( px < image.nx * factor ) {
                    if ( py < image.ny * factor ) {
                        if ( Kernel.getLocalProvider().getImagePath( _id , page , level , px , py ) == null ) {
                            Kernel.getRemoteProvider().getImage( getApplicationContext() , new DownloadReceiver() {
                                @Override
                                public void receive( final Void result ) {
                                    ensureImage( image , nLevel , page , level , px , py + 1 );
                                }
                            } , _id , page , level , px , py , getShortWidth() ).execute();
                        } else {
                            // for stack over flow :(
                            new Handler().post( new Runnable() {
                                @Override
                                public void run() {
                                    ensureImage( image , nLevel , page , level , px , py + 1 );
                                }
                            } );
                        }
                    } else {
                        ensureImage( image , nLevel , page , level , px + 1 , 0 );
                    }
                } else {
                    ensureImage( image , nLevel , page , level + 1 , 0 , 0 );
                }
            } else {
                getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_PROGRESS ). //
                        putExtra( EXTRA_PAGE , page + 1 ). //
                        putExtra( EXTRA_PAGES , _doc.pages ) );

                ensureImage( image , nLevel , page + 1 , 0 , 0 , 0 );
            }
        } else {
            Kernel.getLocalProvider().setCompleted( _id );
            Kernel.getAppStateManager().setDownloadTarget( -1 );

            stopSelf();
        }
    }

    private void ensureImageInfo() {
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( _id );

        if ( image == null ) {
            Kernel.getRemoteProvider().getImageInfo( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    ensureImageLevel( Kernel.getLocalProvider().getImageInfo( _id ) );
                }
            } , _id ).execute();
        } else {
            ensureImageLevel( image );
        }
    }

    private void ensureImageLevel( final ImageInfo image ) {
        final int level = Kernel.getLocalProvider().getImageLevel( _id );

        if ( level == -1 ) {
            Kernel.getRemoteProvider().getImageLevel( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    ensureImage( image , Kernel.getLocalProvider().getImageLevel( _id ) , 0 , 0 , 0 , 0 );
                }
            } , _id , getShortWidth() ).execute();
        } else {
            ensureImage( image , level , 0 , 0 , 0 , 0 );
        }
    }

    public int getShortWidth() {
        return Math.min( getResources().getDisplayMetrics().widthPixels ,
                getResources().getDisplayMetrics().heightPixels );
    }

    @Override
    public IBinder onBind( final Intent intent ) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _id = Kernel.getAppStateManager().getDownloadTarget();

        ensureDocInfo();
    }
}
