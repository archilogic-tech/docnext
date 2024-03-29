package jp.archilogic.docnext.android.service;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.FileReceiver;
import jp.archilogic.docnext.android.type.TaskErrorType;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service {
    private class DownloadReceiver implements FileReceiver< Void > {
        @Override
        public void error( final TaskErrorType error ) {
            getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_FAILED ). //
                    putExtra( EXTRA_ERROR , error ) );
        }

        @Override
        public void downloadComplete() {
        }

        @Override
        public void receive( final Void result ) {
        }
    }

    private static final String PREFIX = DownloadService.class.getName();

    public static final String BROADCAST_DOWNLOAD_PROGRESS = PREFIX + ".download.progress";
    public static final String BROADCAST_DOWNLOAD_FAILED = PREFIX + ".download.failed";
    public static final String BROADCAST_DOWNLOAD_DOWNLOADED = PREFIX + ".download.downloaded";

    public static final String EXTRA_CURRENT = PREFIX + ".extra.current";
    public static final String EXTRA_TOTAL = PREFIX + ".extra.total";
    public static final String EXTRA_ITEM_PER_PAGE = PREFIX + ".extra.itemPerPage";
    public static final String EXTRA_ERROR = PREFIX + ".extra.error";

    public static final String EXTRA_PAGE = PREFIX + ".extra.page";
    public static final String EXTRA_LEVEL = PREFIX + ".extra.level";
    public static final String EXTRA_PX = PREFIX + ".extra.px";
    public static final String EXTRA_PY = PREFIX + ".extra.py";
    
    private long _id;
    private DocInfo _doc;

    private int calcImagesPerPage( final ImageInfo image ) {
        final int TEXTURE_SIZE = 512;

        int ret = 0;

        for ( int level = 0 ; level < image.nLevel ; level++ ) {
            final int factor = ( int ) Math.pow( 2 , level );

            final int nx = ( image.width * factor - 1 ) / TEXTURE_SIZE + 1;
            final int ny = ( image.height * factor - 1 ) / TEXTURE_SIZE + 1;

            ret += nx * ny;
        }

        return ret;
    }

    private void checkDockInfo( final int index ) {
        if ( index < _doc.types.length ) {
            switch ( _doc.types[ index ] ) {
            case IMAGE:
                ensureImageInfo( index );
                downloadThumbnail( _id , 0 );
                downloadTOC( _id );
                downloadSinglePages( _id );
                break;
            case TEXT:
                ensureFont( index );
                break;
            default:
                throw new RuntimeException();
            }
        } else {
            Kernel.getLocalProvider().setCompleted( _id );
            Kernel.getAppStateManager().setDownloadTarget( -1 );

            stopSelf();
        }
    }

    private void ensureDocInfo() {
        _doc = Kernel.getLocalProvider().getDocInfo( _id );

        if ( _doc == null ) {
            Kernel.getRemoteProvider().getDocInfo( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    _doc = Kernel.getLocalProvider().getDocInfo( _id );

                    checkDockInfo( 0 );
                }
            } , _id ).execute();
        } else {
            checkDockInfo( 0 );
        }
    }

    private void ensureFont( final int index ) {
        final String path = Kernel.getLocalProvider().getFontPath( "default" );

        if ( path == null ) {
            Kernel.getRemoteProvider().getFont( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    ensureText( index , 0 );
                }
            } , "default" ).execute();
        } else {
            ensureText( index , 0 );
        }
    }

    private void ensureImage( final int index , final ImageInfo image , final int page , final int level ,
            final int px , final int py , final int current , final int imagePerPage ) {
        final int TEXTURE_SIZE = 512;

        if ( page < _doc.pages ) {
            if ( level < image.nLevel ) {
                final int factor = ( int ) Math.pow( 2 , level );

                if ( px < ( image.width * factor - 1 ) / TEXTURE_SIZE + 1 ) {
                    if ( py < ( image.height * factor - 1 ) / TEXTURE_SIZE + 1 ) {
                        if ( Kernel.getLocalProvider().getImagePath( _id , page , level , px , py ) == null ) {
                            Kernel.getRemoteProvider().getImage( getApplicationContext() , new DownloadReceiver() {
                                @Override
                                public void receive( final Void result ) {
                                    getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_PROGRESS ). //
                                            putExtra( EXTRA_CURRENT , current + 1 ). //
                                            putExtra( EXTRA_TOTAL , _doc.pages * imagePerPage ). //
                                            putExtra( EXTRA_ITEM_PER_PAGE , imagePerPage ) );

                                    ensureImage( index , image , page , level , px , py + 1 , current + 1 ,
                                            imagePerPage );
                                }
                                
                                @Override
                                public void downloadComplete() {
                                    getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_DOWNLOADED). //
                                            putExtra( EXTRA_PAGE , page ).
                                            putExtra( EXTRA_LEVEL , level ).
                                            putExtra( EXTRA_PX , px ).
                                            putExtra( EXTRA_PY , py ) );
                                }
                            } , _id , page , level , px , py , getShortSide() ).execute();
                        } else {
                            // for stack over flow :(
                            new Handler().post( new Runnable() {
                                @Override
                                public void run() {
                                    getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_PROGRESS ). //
                                            putExtra( EXTRA_CURRENT , current + 1 ). //
                                            putExtra( EXTRA_TOTAL , _doc.pages * imagePerPage ). //
                                            putExtra( EXTRA_ITEM_PER_PAGE , imagePerPage ) );

                                    ensureImage( index , image , page , level , px , py + 1 , current + 1 ,
                                            imagePerPage );
                                }
                            } );
                        }
                    } else {
                        ensureImage( index , image , page , level , px + 1 , 0 , current , imagePerPage );
                    }
                } else {
                    ensureImage( index , image , page , level + 1 , 0 , 0 , current , imagePerPage );
                }
            } else {
                ensureImage( index , image , page + 1 , 0 , 0 , 0 , current , imagePerPage );
            }
        } else {
            checkDockInfo( index + 1 );
        }
    }

    private void ensureImageInfo( final int index ) {
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( _id );

        if ( image == null ) {
            Kernel.getRemoteProvider().getImageInfo( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    final ImageInfo _image = Kernel.getLocalProvider().getImageInfo( _id );

                    ensureImage( index , _image , 0 , 0 , 0 , 0 , 0 , calcImagesPerPage( _image ) );
                }
            } , _id , getShortSide() ).execute();
        } else {
            ensureImage( index , image , 0 , 0 , 0 , 0 , 0 , calcImagesPerPage( image ) );
        }
    }

    private void ensureText( final int index , final int page ) {
        if ( page < _doc.pages ) {
            Kernel.getRemoteProvider().getText( getApplicationContext() , new DownloadReceiver() {
                @Override
                public void receive( final Void result ) {
                    getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_PROGRESS ). //
                            putExtra( EXTRA_CURRENT , page + 1 ). //
                            putExtra( EXTRA_TOTAL , _doc.pages ). //
                            putExtra( EXTRA_ITEM_PER_PAGE , 1 ) );

                    ensureText( index , page + 1 );
                }
            } , _id , page ).execute();
        } else {
            checkDockInfo( index + 1 );
        }
    }
    
    private void downloadSinglePages( final long id ) {
        DownloadTask task = Kernel.getRemoteProvider().getSinglePages( getApplicationContext() , new DownloadReceiver() {}, id );
        task.execute();
    }

    private void downloadTOC( final long id ) {
        Kernel.getRemoteProvider().getTableOfContentsInfo( getApplicationContext() , new DownloadReceiver() {
            @Override
            public void receive( final Void result ) {
                Log.d( "DownloadService" , "downloaded TOC");
            }
        }, id ).execute();
    }
    
    private void downloadThumbnail( final long id , final int page ) {
        DocInfo doc = Kernel.getLocalProvider().getDocInfo( _id );

        if ( page < doc.pages ) {
            if ( Kernel.getLocalProvider().getThumbnailPath( _id , page ) != null ) {
                downloadThumbnail( id , page + 1 );
            } else {
                Kernel.getRemoteProvider()
                        .getThumbnail( getApplicationContext() , new DownloadReceiver() {
                            @Override
                            public void receive( final Void result ) {
                                downloadThumbnail( id , page + 1 );
                            }
                        } , id , page ).execute();
            }
        }
    }

    public int getShortSide() {
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

        getApplicationContext().sendBroadcast( new Intent( BROADCAST_DOWNLOAD_PROGRESS ). //
                putExtra( EXTRA_CURRENT , 0 ). //
                putExtra( EXTRA_TOTAL , Integer.MAX_VALUE ). //
                putExtra( EXTRA_ITEM_PER_PAGE , 1 ) );

        ensureDocInfo();
    }
}
