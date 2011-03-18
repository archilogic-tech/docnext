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

    public String getImagePath( final long id , final int page , final int level , final int px , final int py ) {
        return String.format( "%simage/%d/%d_%d_%d_%d.jpg" , Config.ENDPOINT , id , page , level , px , py );
    }

    public String getTextInfoPath( final long id , final int page ) {
        return Config.ENDPOINT + "text/" + id + "/" + page + ".json";
    }

    @Deprecated
    public String getThumbnailPath( final long id , final int page ) {
        return Config.ENDPOINT + "thumb/" + id + "/" + page + ".jpg";
    }
}
