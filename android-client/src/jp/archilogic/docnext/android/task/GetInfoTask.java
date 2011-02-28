package jp.archilogic.docnext.android.task;

import java.io.IOException;

import android.content.Context;

public class GetInfoTask extends NetworkTask< Void , String > {
    private final Receiver< String , Void > _receiver;
    private final String _url;

    public GetInfoTask( final Context context , final Receiver< String , Void > receiver , final String url ) {
        super( context );

        _receiver = receiver;
        _url = url;
    }

    @Override
    protected String background() throws IOException {
        // final FileOutputStream out = new FileOutputStream( StorageUtil.getFontPath( _url ) );

        // IOUtils.copy( NetUtil.getFont( _name ) , out );

        // out.close();

        return null;
    }

    @Override
    protected void onNetworkError() {
        _receiver.error( null );
    }

    @Override
    protected void post( final String result ) {
        _receiver.receive( result );
    }
}
