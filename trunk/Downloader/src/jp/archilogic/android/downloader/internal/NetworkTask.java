package jp.archilogic.android.downloader.internal;

import java.io.IOException;

import jp.archilogic.android.downloader.DownloadError;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public abstract class NetworkTask< Progress , Result > extends AsyncTask< Void , Progress , Result > {
    private final Context _context;

    private boolean _networkUnavailable = false;
    private boolean _networkError = false;

    public NetworkTask( final Context context ) {
        _context = context;
    }

    protected abstract Result background() throws IOException;

    @Override
    protected final Result doInBackground( final Void ... params ) {
        if ( !isNetworkAvaiable() ) {
            _networkUnavailable = true;
            return null;
        }

        try {
            return background();
        } catch ( final IOException e ) {
            Log.d( "downloader" , "NetworkError: " + e.getMessage() );

            _networkError = true;
            return null;
        }
    }

    private boolean isNetworkAvaiable() {
        final ConnectivityManager manager = ( ConnectivityManager ) _context.getSystemService( Context.CONNECTIVITY_SERVICE );

        final NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    protected abstract void onNetworkError( DownloadError error );

    @Override
    protected final void onPostExecute( final Result result ) {
        super.onPostExecute( result );

        if ( _networkUnavailable ) {
            onNetworkError( DownloadError.NETWORK_UNAVAILABLE );

            return;
        }

        if ( _networkError ) {
            onNetworkError( DownloadError.NETWORK_ERROR );

            return;
        }

        post( result );
    }

    protected abstract void post( Result result );
}
