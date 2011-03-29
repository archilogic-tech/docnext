package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.Config;

public class RemotePathManager {
    public String getDocInfoPath( final long id ) {
        return Config.ENDPOINT + "viewer/getDocInfo?id=" + id;
    }

    public String getFontPath( final String name ) {
        return Config.ENDPOINT + "font/" + name;
    }

    public String getImageInfoPath( final long id , final int shortSide ) {
        return Config.ENDPOINT + "viewer/getImageInfo?id=" + id + "&shortSide=" + shortSide;
    }

    public String getImagePath( final long id , final int page , final int level , final int px , final int py ,
            final int shortSide ) {
        return String.format( "%sviewer/smartGetPage?id=%d&page=%d&level=%d&px=%d&py=%d&shortSide=%d" ,
                Config.ENDPOINT , id , page , level , px , py , shortSide );
    }

    public String getTextPath( final long id , final int page ) {
        return String.format( "%sviewer/getText?id=%d&page=%d" , Config.ENDPOINT , id , page );
    }
}
