package jp.archilogic.docnext.android.coreview.image;

public class PageInfo {
    enum PageTextureStatus {
        UNBIND , BIND;
    }

    int width;
    int height;
    PageTextureInfo[][][] textures;
    PageTextureStatus[][][] statuses;
}
