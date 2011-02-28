package jp.archilogic.docnext.android.task;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.archilogic.docnext.android.util.NetUtil;
import net.arnx.jsonic.JSON;
import android.os.AsyncTask;

public class DownloadTask extends AsyncTask< Void , Void , Integer > {
    private final long _id;
    private final IntegerReceiver _receiver;

    public DownloadTask( final long id , final IntegerReceiver receiver ) {
        _id = id;
        _receiver = receiver;
    }

    @Override
    protected Integer doInBackground( final Void ... params ) {
        try {
            final ZipInputStream in = new ZipInputStream( NetUtil.download( _id ) );

            for ( ZipEntry entry = in.getNextEntry() ; entry != null ; entry = in.getNextEntry() ) {
                if ( entry.getName().equals( "info.json" ) ) {
                    final Map< String , Object > info = JSON.decode( in );
                    return ( ( BigDecimal ) info.get( "pages" ) ).intValue();
                }
            }

            return null;
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected void onPostExecute( final Integer result ) {
        super.onPostExecute( result );

        _receiver.receiver( result );
    }
}
