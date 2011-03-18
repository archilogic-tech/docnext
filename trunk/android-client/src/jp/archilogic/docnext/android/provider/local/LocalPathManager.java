package jp.archilogic.docnext.android.provider.local;

import java.io.File;

public class LocalPathManager {
    private final String ROOT = "/sdcard/docnext/";

    private void ensure( final String path ) {
        final File dir = new File( path );

        if ( !dir.exists() ) {
            dir.mkdirs();
        }
    }

    public void ensureDocInfoDir() {
        ensure( getDocInfoDir() );
    }

    public void ensureFontDir() {
        ensure( getFontDir() );
    }

    public void ensureImageDir( final long id ) {
        ensure( getImageDir( id ) );
    }

    public void ensureTextInfoDir( final long id ) {
        ensure( getTextInfoDir( id ) );
    }

    public void ensureThumbDir( final long id ) {
        ensure( getThumbDir( id ) );
    }

    public String getCompletedInfoPath() {
        return ROOT + "completed.json";
    }

    private String getDocInfoDir() {
        return ROOT + "meta/";
    }

    public String getDocInfoPath( final long id ) {
        return getDocInfoDir() + id + ".json";
    }

    private String getFontDir() {
        return ROOT + "font/";
    }

    public String getFontPath( final String name ) {
        return getFontDir() + name;
    }

    private String getImageDir( final long id ) {
        return ROOT + "image/" + id + "/";
    }

    public String getImageInfoPath( final long id ) {
        return getDocInfoDir() + id + ".image.json";
    }

    public String getImagePath( final long id , final int page , final int level , final int px , final int py ) {
        return String.format( "%s%d_%d_%d_%d.jpg" , getImageDir( id ) , page , level , px , py );
    }

    private String getTextInfoDir( final long id ) {
        return ROOT + "text/" + id + "/";
    }

    public String getTextInfoPath( final long id , final int page ) {
        return getTextInfoDir( id ) + page + ".json";
    }

    @Deprecated
    private String getThumbDir( final long id ) {
        return ROOT + "thumb/" + id + "/";
    }

    @Deprecated
    public String getThumbnailPath( final long id , final int page ) {
        return getThumbDir( id ) + page + ".jpg";
    }
}