package jp.archilogic.docnext.android.task;

import java.io.FileOutputStream;
import java.io.IOException;

import jp.archilogic.docnext.android.util.NetUtil;
import jp.archilogic.docnext.android.util.StorageUtil;

import org.apache.commons.io.IOUtils;

import android.content.Context;

public class GetImagePageThumbnailTask extends NetworkTask< Void , Void > {
    private final Receiver< Void , Void > _receiver;
    private final long _id;
    private final int _page;

    public GetImagePageThumbnailTask( final Context context , final Receiver< Void , Void > receiver , final long id ,
            final int page ) {
        super( context );

        _receiver = receiver;
        _id = id;
        _page = page;
    }

    @Override
    protected Void background() throws IOException {
        final FileOutputStream out = new FileOutputStream( StorageUtil.getImageThumbnailPath( _id , _page ) );

        IOUtils.copy( NetUtil.getImagePageThumbnail( _id , _page ) , out );

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
