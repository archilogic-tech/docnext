package jp.archilogic.docnext.android.task;

import java.io.FileOutputStream;
import java.io.IOException;

import jp.archilogic.docnext.android.util.NetUtil;

import org.apache.commons.io.IOUtils;

import android.content.Context;

public class DownloadTask extends NetworkTask< Void , Void > {
    private final Receiver< Void , Void > _receiver;
    private final String _remotePath;
    private final String _localPath;

    public DownloadTask( final Context context , final Receiver< Void , Void > receiver , final String remotePath ,
            final String localPath ) {
        super( context );

        _receiver = receiver;
        _remotePath = remotePath;
        _localPath = localPath;
    }

    @Override
    protected Void background() throws IOException {
        final FileOutputStream out = new FileOutputStream( _localPath );

        IOUtils.copy( NetUtil.get( _remotePath ) , out );

        out.close();

        return null;
    }

    @Override
    protected void onNetworkError() {
        _receiver.error( null );
    }

    @Override
    protected void post( final Void result ) {
        _receiver.receive( result );
    }
}
