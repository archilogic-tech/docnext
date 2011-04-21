package jp.archilogic.docnext.android.task;

import java.io.FileOutputStream;
import java.io.IOException;

import jp.archilogic.docnext.android.type.TaskErrorType;
import jp.archilogic.docnext.android.util.NetUtil;

import org.apache.commons.io.IOUtils;

import android.content.Context;

public class DownloadTask extends NetworkTask< Void , Void > {
    private final Receiver< Void > _receiver;
    private final String _remotePath;
    private final String _localPath;

    public DownloadTask( final Context context , final Receiver< Void > receiver , final String remotePath ,
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

        IOUtils.closeQuietly( out );

        onDownloaded();
        
        return null;
    }
    
    public void onDownloaded() {
        _receiver.downloaded();
    }

    @Override
    protected void onNetworkError( final TaskErrorType error ) {
        _receiver.error( error );
    }

    @Override
    protected void post( final Void result ) {
        _receiver.receive( result );
    }
}
