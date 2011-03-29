package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.provider.local.LocalPathManager;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.Receiver;
import android.content.Context;

public class RemoteProviderImpl implements RemoteProvider {
    private final RemotePathManager _remotePathManager = new RemotePathManager();
    private final LocalPathManager _localPathManager = new LocalPathManager();

    @Override
    public DownloadTask getDocInfo( final Context context , final Receiver< Void > receiver , final long id ) {
        _localPathManager.ensureDocInfoDir();

        return new DownloadTask( context , receiver , _remotePathManager.getDocInfoPath( id ) ,
                _localPathManager.getDocInfoPath( id ) );
    }

    @Override
    public DownloadTask getFont( final Context context , final Receiver< Void > receiver , final String name ) {
        _localPathManager.ensureFontDir();

        return new DownloadTask( context , receiver , _remotePathManager.getFontPath( name ) ,
                _localPathManager.getFontPath( name ) );
    }

    @Override
    public DownloadTask getImage( final Context context , final Receiver< Void > receiver , final long id ,
            final int page , final int level , final int px , final int py , final int shortSide ) {
        _localPathManager.ensureImageDir( id );

        return new DownloadTask( context , receiver , _remotePathManager.getImagePath( id , page , level , px , py ,
                shortSide ) , _localPathManager.getImagePath( id , page , level , px , py ) );
    }

    @Override
    public DownloadTask getImageInfo( final Context context , final Receiver< Void > receiver , final long id ,
            final int shortSide ) {
        _localPathManager.ensureDocInfoDir();

        return new DownloadTask( context , receiver , _remotePathManager.getImageInfoPath( id , shortSide ) ,
                _localPathManager.getImageInfoPath( id ) );
    }

    @Override
    public DownloadTask getText( final Context context , final Receiver< Void > receiver , final long id ,
            final int page ) {
        _localPathManager.ensureTextInfoDir( id );

        return new DownloadTask( context , receiver , _remotePathManager.getTextPath( id , page ) ,
                _localPathManager.getTextPath( id , page ) );
    }
}
