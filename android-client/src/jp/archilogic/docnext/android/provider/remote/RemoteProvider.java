package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.Receiver;
import android.content.Context;

/**
 * @@ Download process is handled by root activity (or service?)
 * @@ Notify process finishing is archived through BroadCast
 */
public interface RemoteProvider {
    DownloadTask getDocInfo( Context context , Receiver< Void > receiver , long id );

    DownloadTask getFont( Context context , Receiver< Void > receiver , String name );

    DownloadTask getImage( Context context , Receiver< Void > receiver , long id , int page , int level , int px ,
            int py , int shortSide );

    DownloadTask getImageInfo( Context context , Receiver< Void > receiver , long id , int shortSide );
    
    DownloadTask getSinglePages( Context context , Receiver< Void > receiver , long id );

    DownloadTask getText( Context context , Receiver< Void > receiver , long id , int page );

	DownloadTask getTableOfContentsInfo( Context context, Receiver< Void > receiver, long id );

    DownloadTask getThumbnail( Context context , Receiver< Void > receiver , long id , int page );
}
