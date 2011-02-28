package jp.archilogic.docnext.android.task;

import java.io.IOException;

import jp.archilogic.docnext.android.R;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class NetworkTask< Progress , Result > extends AsyncTask< Void , Progress , Result > {
    private final int _networkUnavailableMessage = R.string.network_unavailable;
    private final int _networkErrorMessage = R.string.network_error;

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
            _networkError = true;
            return null;
        }
    }

    private boolean isNetworkAvaiable() {
        final ConnectivityManager manager =
                ( ConnectivityManager ) _context.getSystemService( Context.CONNECTIVITY_SERVICE );

        final NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    protected abstract void onNetworkError();

    @Override
    protected final void onPostExecute( final Result result ) {
        super.onPostExecute( result );

        if ( _networkUnavailable ) {
            Toast.makeText( _context , _networkUnavailableMessage , Toast.LENGTH_LONG ).show();

            onNetworkError();

            return;
        }

        if ( _networkError ) {
            Toast.makeText( _context , _networkErrorMessage , Toast.LENGTH_LONG ).show();

            onNetworkError();

            return;
        }

        post( result );
    }

    protected abstract void post( Result result );
}