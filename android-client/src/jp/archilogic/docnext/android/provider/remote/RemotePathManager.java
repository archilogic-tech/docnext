package jp.archilogic.docnext.android.provider.remote;

import jp.archilogic.docnext.android.Kernel;

public class RemotePathManager {
    public String getFontPath( final String name ) {
        return Kernel.ENDPOINT + "font/" + name;
    }

    public String getImagePath( final long id , final int page , final int level , final int px , final int py ) {
        return Kernel.ENDPOINT + "image/" + id + "/" + page + ".jpg";
    }

    public String getMetaInfoPath( final long id ) {
        return Kernel.ENDPOINT + "meta/" + id + ".json";
    }

    public String getTextInfoPath( final long id , final int page ) {
        return Kernel.ENDPOINT + "text/" + id + "/" + page + ".json";
    }

    public String getThumbnailPath( final long id , final int page ) {
        return Kernel.ENDPOINT + "thumb/" + id + "/" + page + ".jpg";
    }
}
