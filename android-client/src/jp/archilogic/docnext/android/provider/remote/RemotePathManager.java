package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.Config;

public class RemotePathManager {
    public String getDocInfoPath( final long id ) {
        return String.format( "%sviewer/getDocInfo/%d" , Config.ENDPOINT , id );
    }

    public String getFontPath( final String name ) {
        return String.format( "%sviewer/getFont/%s" , Config.ENDPOINT , name );
    }

    public String getImageInfoPath( final long id , final int shortSide ) {
        return String.format( "%sviewer/getImageInfo/%d/%d" , Config.ENDPOINT , id , shortSide );
    }

    public String getImagePath( final long id , final int page , final int level , final int px ,
            final int py , final int shortSide ) {
        return String.format( "%sviewer/smartGetPage/%d/%d/%d/%d/%d/%d" , Config.ENDPOINT , id ,
                page , level , px , py , shortSide );
    }

    public String getTableOfContentsPath( final long id ) {
        return String.format( "%sviewer/getTOC/%d" , Config.ENDPOINT , id );
    }

    public String getTextPath( final long id , final int page ) {
        return String.format( "%sviewer/getText/%d/%d" , Config.ENDPOINT , id , page );
    }

    public String getThumbnailPath( final long id , final int page ) {
        return String.format( "%sviewer/getThumbnail/%d/%d" , Config.ENDPOINT , id , page );
    }
}
