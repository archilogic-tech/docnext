package jp.archilogic.docnext.android.provider.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class AppStateManager {
    LocalPathManager _pathManager = new LocalPathManager();

    public long getDownloadTarget() {
        try {
            return Long.valueOf( FileUtils.readFileToString( new File( _pathManager.getDownloadTargetPath() ) ).trim() );
        } catch ( final IOException e ) {
            return -1;
        }
    }

    public void setDownloadTarget( final long id ) {
        try {
            _pathManager.ensureRoot();

            FileUtils.writeStringToFile( new File( _pathManager.getDownloadTargetPath() ) , Long.toString( id ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
