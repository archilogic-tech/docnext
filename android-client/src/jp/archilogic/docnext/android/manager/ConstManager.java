package jp.archilogic.docnext.android.manager;

import java.io.IOException;
import java.io.InputStream;

import net.arnx.jsonic.JSON;
import android.content.Context;

public class ConstManager {
    private static class ServerInfo {
        public String endpoint;
    }

    private static ConstManager _instance = null;

    public static ConstManager getInstance( final Context context ) {
        if ( _instance == null ) {
            _instance = new ConstManager();
            _instance.load( context );
        }

        return _instance;
    }

    private ServerInfo _info;

    private ConstManager() {
    }

    public String getEndpoint() {
        return _info.endpoint;
    }

    public void load( final Context context ) {
        try {
            final InputStream in = context.getAssets().open( "server.json" );

            _info = JSON.decode( in , ServerInfo.class );

            in.close();
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
