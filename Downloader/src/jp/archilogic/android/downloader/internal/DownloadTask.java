package jp.archilogic.android.downloader.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.archilogic.android.downloader.DownloadError;
import jp.archilogic.android.downloader.internal.NetUtil.PartialStreamWithLength;
import jp.archilogic.android.downloader.internal.NetUtil.StreamWithLength;

import org.apache.commons.io.IOUtils;

import android.content.Context;

public class DownloadTask extends NetworkTask< Void , Void > {
    private final DownloadReceiver _receiver;
    private final String _remotePath;
    private final String _localPath;
    private final boolean _tryResume;

    public DownloadTask( final Context context , final DownloadReceiver receiver , final String remotePath , final String localPath ,
            final boolean tryResume ) {
        super( context );

        _receiver = receiver;
        _remotePath = remotePath;
        _localPath = localPath;
        _tryResume = tryResume;
    }

    @Override
    protected Void background() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            final long length = new File( _localPath ).length();
            long count;

            if ( length == 0 || !_tryResume ) {
                final StreamWithLength res = NetUtil.get( _remotePath );
                in = res.getInputStream();

                out = new FileOutputStream( _localPath );
                count = 0;

                _receiver.contentLength( res.getContentLength() );
            } else {
                final PartialStreamWithLength res = NetUtil.get( _remotePath , length );
                in = res.getInputStream();

                if ( res.isPartial() ) {
                    out = new FileOutputStream( _localPath , true );
                    count = length;
                } else {
                    out = new FileOutputStream( _localPath , false );
                    count = 0;
                }
            }

            int n = 0;
            for ( final byte[] buffer = new byte[ 1024 * 256 ] ; ( n = in.read( buffer ) ) != -1 ; ) {
                out.write( buffer , 0 , n );
                count += n;

                _receiver.progress( count );
            }

            return null;
        } finally {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( in );
        }
    }

    @Override
    protected void onNetworkError( final DownloadError error ) {
        _receiver.error( error );
    }

    @Override
    protected void post( final Void result ) {
        _receiver.receive( result );
    }
}
