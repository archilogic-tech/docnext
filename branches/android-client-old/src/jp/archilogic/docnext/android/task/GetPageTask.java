package jp.archilogic.docnext.android.task;

import jp.archilogic.docnext.android.util.NetUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class GetPageTask extends AsyncTask< Void , Void , Bitmap > {
    private final long _id;
    private final int _page;
    private final int _level;
    private final int _px;
    private final int _py;
    private final BitmapReceiver _receiver;

    public GetPageTask( final long id , final int page , final int level , final int px , final int py ,
            final BitmapReceiver receiver ) {
        _id = id;
        _page = page;
        _level = level;
        _px = px;
        _py = py;
        _receiver = receiver;
    }

    @Override
    protected Bitmap doInBackground( final Void ... params ) {
        return BitmapFactory.decodeStream( NetUtil.getPage( _id , _page , _level , _px , _py ) );
    }

    @Override
    protected void onPostExecute( final Bitmap result ) {
        super.onPostExecute( result );

        _receiver.receiver( result );
    }
}
