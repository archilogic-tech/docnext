package jp.archilogic.android.downloader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.os.Bundle;
import android.os.Parcel;

/**
 * Value object to specify download target
 */
@SuppressWarnings( "serial" )
public class DownloadInfo implements Serializable {
    private String _remotePath;
    private String _localPath;
    private Bundle _extra;
    private long _estimatedSize;

    /**
     * @param remotePath
     *            Path to remote file
     * @param localPath
     *            Path to local file
     * @param extra
     *            Extra which broadcast with Downloader.ACTION_DOWNLOADED etc
     */
    public DownloadInfo( final String remotePath , final String localPath , final Bundle extra ) {
        _remotePath = remotePath;
        _localPath = localPath;
        _extra = extra;
        _estimatedSize = -1;
    }

    /**
     * @param remotePath
     *            Path to remote file
     * @param localPath
     *            Path to local file
     * @param extra
     *            Extra which broadcast with Downloader.ACTION_DOWNLOADED etc
     * @param estimatedSize
     *            Use for notification progress bar calculation (currently not used)
     */
    public DownloadInfo( final String remotePath , final String localPath , final Bundle extra , final long estimatedSize ) {
        _remotePath = remotePath;
        _localPath = localPath;
        _extra = extra;
        _estimatedSize = estimatedSize;
    }

    public long getEstimatedSize() {
        return _estimatedSize;
    }

    public Bundle getExtra() {
        return _extra;
    }

    public String getLocalPath() {
        return _localPath;
    }

    public String getRemotePath() {
        return _remotePath;
    }

    private void readObject( final ObjectInputStream in ) throws IOException , ClassNotFoundException {
        _remotePath = in.readUTF();
        _localPath = in.readUTF();

        final int dataLength = in.readInt();
        final byte[] data = new byte[ dataLength ];
        in.read( data );
        final Parcel extra = Parcel.obtain();
        extra.unmarshall( data , 0 , dataLength );
        extra.setDataPosition( 0 );
        _extra = extra.readBundle();

        _estimatedSize = in.readLong();
    }

    private void writeObject( final ObjectOutputStream out ) throws IOException {
        out.writeUTF( _remotePath );
        out.writeUTF( _localPath );

        final Parcel extra = Parcel.obtain();
        _extra.writeToParcel( extra , 0 );
        final byte[] data = extra.marshall();
        out.writeInt( data.length );
        out.write( data );

        out.writeLong( _estimatedSize );
    }
}
