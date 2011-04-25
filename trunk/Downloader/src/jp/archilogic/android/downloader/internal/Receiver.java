package jp.archilogic.android.downloader.internal;

import jp.archilogic.android.downloader.DownloadError;

public interface Receiver< T > {
    void error( DownloadError error );

    void receive( T result );
}
