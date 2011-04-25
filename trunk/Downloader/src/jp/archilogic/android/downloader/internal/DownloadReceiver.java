package jp.archilogic.android.downloader.internal;

public interface DownloadReceiver extends Receiver< Void > {
    void contentLength( long contentLength );

    void progress( long length );
}
