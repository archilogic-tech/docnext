package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.Config;

public class RemotePathManager {
    public String getDocInfoPath( final long id ) {
        return Config.ENDPOINT + "meta/" + id + ".json";
    }

    public String getFontPath( final String name ) {
        return Config.ENDPOINT + "font/" + name;
    }

    public String getImageInfoPath( final long id ) {
        return Config.ENDPOINT + "meta/" + id + ".image.json";
    }

    public String getImageLevelPath( final long id , final int shortWidth ) {
        return Config.ENDPOINT + "meta/" + id + ".image.level.txt?shortWidth=" + shortWidth;
    }

    public String getImagePath( final long id , final int page , final int level , final int px , final int py ,
            final int shortWidth ) {
        return String.format( "%simage/%d/%d_%d_%d_%d.jpg?shortWidth=%d" , Config.ENDPOINT , id , page , level , px ,
                py , shortWidth );
    }

    public String getTextInfoPath( final long id , final int page ) {
        return Config.ENDPOINT + "text/" + id + "/" + page + ".json";
    }
}
