package jp.archilogic.docnext.dto;

public class ImageInfo {
    public int width;
    public int height;
    public int nLevel;

    public ImageInfo( final int width , final int height , final int nLevel ) {
        this.width = width;
        this.height = height;
        this.nLevel = nLevel;
    }
}
