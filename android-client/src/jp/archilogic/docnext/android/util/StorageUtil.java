package jp.archilogic.docnext.android.util;

import java.io.File;

public class StorageUtil {
    private static final String ROOT = "/sdcard/docnext/";

    public static void ensureFontDir() {
        final File dir = new File( getFontDir() );

        if ( !dir.exists() ) {
            dir.mkdirs();
        }
    }

    public static void ensureImageDir( final long id ) {
        final File dir = new File( getImageDir( id ) );

        if ( !dir.exists() ) {
            dir.mkdirs();
        }
    }

    private static String getFontDir() {
        return ROOT + "font/";
    }

    public static String getFontPath( final String name ) {
        return getFontDir() + name;
    }

    private static String getImageDir( final long id ) {
        return ROOT + "image/" + id + "/";
    }

    public static String getImagePath( final long id , final int page ) {
        return getImageDir( id ) + String.format( "%03d.jpg" , page );
    }

    public static String getImageThumbnailPath( final long id , final int page ) {
        return getImageDir( id ) + String.format( "%03d-thumb.jpg" , page );
    }
}
