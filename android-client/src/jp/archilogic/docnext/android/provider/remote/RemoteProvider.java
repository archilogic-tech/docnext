package jp.archilogic.docnext.android.provider.remote;

import android.content.Context;

/**
 * @@ Download process is handled by root activity (or service?)
 * @@ Notify process finishing is archived through BroadCast
 */
public interface RemoteProvider {
    final String PACKAGE_NAME = "jp.archilogic.docnext.android.provider.remote";

    final String BROADCAST_GET_DOC_INFO_SUCCEED = PACKAGE_NAME + ".method.getDocInfo.succeed";
    final String BROADCAST_GET_DOC_INFO_FAILED = PACKAGE_NAME + ".method.getDocInfo.failed";
    final String BROADCAST_GET_FONT_SUCCEED = PACKAGE_NAME + ".method.getFont.succeed";
    final String BROADCAST_GET_FONT_FAILED = PACKAGE_NAME + ".method.getFont.failed";
    final String BROADCAST_GET_IMAGE_SUCCEED = PACKAGE_NAME + ".method.getImage.succeed";
    final String BROADCAST_GET_IMAGE_FAILED = PACKAGE_NAME + ".method.getImage.failed";
    final String BROADCAST_GET_TEXT_INFO_SUCCEED = PACKAGE_NAME + ".method.getTextInfo.succeed";
    final String BROADCAST_GET_TEXT_INFO_FAILED = PACKAGE_NAME + ".method.getTextInfo.failed";
    final String BROADCAST_GET_THUMBNAIL_SUCCEED = PACKAGE_NAME + ".method.getThumbnail.succeed";
    final String BROADCAST_GET_THUMBNAIL_FAILED = PACKAGE_NAME + ".method.getThumbnail.failed";

    final String EXTRA_ID = PACKAGE_NAME + ".extra.id";
    final String EXTRA_NAME = PACKAGE_NAME + ".extra.name";
    final String EXTRA_PAGE = PACKAGE_NAME + ".extra.page";
    final String EXTRA_LEVEL = PACKAGE_NAME + ".extra.level";
    final String EXTRA_PX = PACKAGE_NAME + ".extra.px";
    final String EXTRA_PY = PACKAGE_NAME + ".extra.py";
    final String EXTRA_ERROR = PACKAGE_NAME + ".extra.error";

    void getDocInfo( Context context , long id );

    void getFont( Context context , String name );

    void getImage( Context context , long id , int page , int level , int px , int py );

    void getTextInfo( Context context , long id , int page );

    void getThumbnail( Context context , long id , int page );

    boolean isWorking();

    void setWorking( boolean working );
}
