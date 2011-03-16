package jp.archilogic.docnext.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.archilogic.docnext.android.info.MetaInfo;
import net.arnx.jsonic.JSON;

public class StorageUtil {
    private static final String ROOT = "/sdcard/docnext/";

    private static void ensureDir( final String path ) {
        final File dir = new File( path );

        if ( !dir.exists() ) {
            dir.mkdirs();
        }
    }

    public static void ensureFontDir() {
        ensureDir( getFontDir() );
    }

    public static void ensureImageDir( final long id ) {
        ensureDir( getImageDir( id ) );
    }

    public static void ensureMetaInfoDir() {
        ensureDir( getMetaInfoDir() );
    }

    public static void ensureTextDir( final long id ) {
        ensureDir( getTextDir( id ) );
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

    public static MetaInfo getMetaInfo( final long id ) {
        InputStream in = null;
        try {
            in = new FileInputStream( getMetaInfoPath( id ) );

            return JSON.decode( in , MetaInfo.class );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        } finally {
            if ( in != null ) {
                try {
                    in.close();
                } catch ( final IOException e ) {
                    throw new RuntimeException( e );
                }
            }
        }
    }

    private static String getMetaInfoDir() {
        return ROOT + "meta/";
    }

    public static String getMetaInfoPath( final long id ) {
        return getMetaInfoDir() + id + ".json";
    }

    private static String getTextDir( final long id ) {
        return ROOT + "text/" + id + "/";
    }

    public static String getTextPath( final long id , final int page ) {
        return getTextDir( id ) + page + ".json";
    }
}
