package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.provider.local.LocalPathManager;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.Receiver;
import jp.archilogic.docnext.android.type.TaskErrorType;
import android.content.Context;
import android.content.Intent;

public class RemoteProviderImpl implements RemoteProvider {
    private final RemotePathManager _remotePathManager = new RemotePathManager();
    private final LocalPathManager _localPathManager = new LocalPathManager();

    private boolean _isWorking = false;

    @Override
    public void getDocInfo( final Context context , final long id ) {
        _localPathManager.ensureDocInfoDir();

        new DownloadTask( context , new Receiver< Void >() {
            @Override
            public void error( final TaskErrorType error ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_DOC_INFO_FAILED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_ERROR , error ) );
            }

            @Override
            public void receive( final Void result ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_DOC_INFO_SUCCEED ).putExtra( EXTRA_ID , id ) );
            }
        } , _remotePathManager.getDocInfoPath( id ) , _localPathManager.getDocInfoPath( id ) ).execute();
    }

    @Override
    public void getFont( final Context context , final String name ) {
        _localPathManager.ensureFontDir();

        new DownloadTask( context , new Receiver< Void >() {
            @Override
            public void error( final TaskErrorType error ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_FONT_FAILED ). //
                        putExtra( EXTRA_NAME , name ). //
                        putExtra( EXTRA_ERROR , error ) );
            }

            @Override
            public void receive( final Void result ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_FONT_SUCCEED ).putExtra( EXTRA_NAME , name ) );
            }
        } , _remotePathManager.getFontPath( name ) , _localPathManager.getFontPath( name ) ).execute();
    }

    @Override
    public void getImage( final Context context , final long id , final int page , final int level , final int px ,
            final int py ) {
        _localPathManager.ensureImageDir( id );

        new DownloadTask( context , new Receiver< Void >() {
            @Override
            public void error( final TaskErrorType error ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_IMAGE_FAILED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_PAGE , page ). //
                        putExtra( EXTRA_LEVEL , level ). //
                        putExtra( EXTRA_PX , px ). //
                        putExtra( EXTRA_PY , py ). //
                        putExtra( EXTRA_ERROR , error ) );
            }

            @Override
            public void receive( final Void result ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_IMAGE_SUCCEED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_PAGE , page ). //
                        putExtra( EXTRA_LEVEL , level ). //
                        putExtra( EXTRA_PX , px ). //
                        putExtra( EXTRA_PY , py ) );
            }
        } , _remotePathManager.getImagePath( id , page , level , px , py ) , _localPathManager.getImagePath( id , page ,
                level , px , py ) ).execute();
    }

    @Override
    public void getImageInfo( final Context context , final long id ) {
        _localPathManager.ensureDocInfoDir();

        new DownloadTask( context , new Receiver< Void >() {
            @Override
            public void error( final TaskErrorType error ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_IMAGE_INFO_FAILED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_ERROR , error ) );
            }

            @Override
            public void receive( final Void result ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_IMAGE_INFO_SUCCEED ).putExtra( EXTRA_ID , id ) );
            }
        } , _remotePathManager.getImageInfoPath( id ) , _localPathManager.getImageInfoPath( id ) ).execute();
    }

    @Override
    public void getTextInfo( final Context context , final long id , final int page ) {
        _localPathManager.ensureTextInfoDir( id );

        new DownloadTask( context , new Receiver< Void >() {
            @Override
            public void error( final TaskErrorType error ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_TEXT_INFO_FAILED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_PAGE , page ). //
                        putExtra( EXTRA_ERROR , error ) );
            }

            @Override
            public void receive( final Void result ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_TEXT_INFO_SUCCEED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_PAGE , page ) );
            }
        } , _remotePathManager.getTextInfoPath( id , page ) , _localPathManager.getTextInfoPath( id , page ) )
                .execute();
    }

    @Override
    @Deprecated
    public void getThumbnail( final Context context , final long id , final int page ) {
        _localPathManager.ensureThumbDir( id );

        new DownloadTask( context , new Receiver< Void >() {
            @Override
            public void error( final TaskErrorType error ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_THUMBNAIL_FAILED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_PAGE , page ). //
                        putExtra( EXTRA_ERROR , error ) );
            }

            @Override
            public void receive( final Void result ) {
                context.sendBroadcast( new Intent( BROADCAST_GET_THUMBNAIL_SUCCEED ). //
                        putExtra( EXTRA_ID , id ). //
                        putExtra( EXTRA_PAGE , page ) );
            }
        } , _remotePathManager.getThumbnailPath( id , page ) , _localPathManager.getThumbnailPath( id , page ) )
                .execute();
    }

    @Override
    public boolean isWorking() {
        return _isWorking;
    }

    @Override
    public void setWorking( final boolean working ) {
        _isWorking = working;
    }
}
