package jp.archilogic.docnext.android.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.archilogic.docnext.android.type.TaskErrorType;
import jp.archilogic.docnext.android.util.NetUtil;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.os.FileObserver;

public class DownloadTask extends NetworkTask< Void , Void > {
    private final Receiver< Void > _receiver;
    private final String _remotePath;
    private final String _localPath;
    private FileObserver _observer;
    
    public static final String DOWNLOADING_POSTFIX = "downloading";

    public DownloadTask( final Context context , final Receiver< Void > receiver , final String remotePath ,
            final String localPath ) {
        super( context );

        _receiver = receiver;
        _remotePath = remotePath;
        _localPath = localPath;

        _observer = new FileObserver( _localPath ) {
            
            @Override
            public void onEvent( int event , String path ) {
                if ( event == CLOSE_WRITE ) {
                    ( new File( _localPath + DOWNLOADING_POSTFIX ) ).delete();
                    
                    onDownloadComplete();
                    
                    this.stopWatching();
                }
            }
        };
    }

    @Override
    protected Void background() throws IOException {
        final FileOutputStream out = new FileOutputStream( _localPath );

        ( new File( _localPath + DOWNLOADING_POSTFIX ) ).createNewFile();
        _observer.startWatching();
        
        IOUtils.copy( NetUtil.get( _remotePath ) , out  );

        IOUtils.closeQuietly( out );
        
        return null;
    }
    
    private void onDownloadComplete() {
        if ( _receiver instanceof FileReceiver ) {
            ( ( FileReceiver< Void > ) _receiver ).downloadComplete();
        }
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
