package jp.archilogic.docnext.android.task;

import java.io.FileOutputStream;
import java.io.IOException;

import jp.archilogic.docnext.android.util.NetUtil;
import jp.archilogic.docnext.android.util.StorageUtil;

import org.apache.commons.io.IOUtils;

import android.content.Context;

public class GetFontTask extends NetworkTask< Void , Void > {
    private final Receiver< Void , Void > _receiver;
    private final String _name;

    public GetFontTask( final Context context , final Receiver< Void , Void > receiver , final String name ) {
        super( context );

        _receiver = receiver;
        _name = name;
    }

    @Override
    protected Void background() throws IOException {
        final FileOutputStream out = new FileOutputStream( StorageUtil.getFontPath( _name ) );

        IOUtils.copy( NetUtil.getFont( _name ) , out );

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
