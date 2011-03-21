package jp.archilogic.docnext.android.provider.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

public class AppStateManager {
    LocalPathManager _pathManager = new LocalPathManager();

    public long getDownloadTarget() {
        final String data = read( _pathManager.getDownloadTargetPath() );

        return data != null ? Long.valueOf( data ) : -1;
    }

    /**
     * @return null if not exists
     */
    private String read( final String path ) {
        BufferedReader reader = null;
        try {
            final File file = new File( path );

            if ( !file.exists() ) {
                return null;
            }

            reader = new BufferedReader( new FileReader( file ) );
            return reader.readLine();
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( reader );
        }
    }

    public void setDownloadTarget( final long id ) {
        _pathManager.ensureRoot();

        write( _pathManager.getDownloadTargetPath() , Long.toString( id ) );
    }

    private void write( final String path , final String data ) {
        try {
            final Writer writer = new FileWriter( path );
            IOUtils.write( data , writer );
            IOUtils.closeQuietly( writer );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
