package jp.archilogic.docnext.android.coreview.image;

public class PageTextureInfo extends TextureInfo {
    int x;
    int y;

    public PageTextureInfo( final int texture , final int width , final int height , final int x , final int y ) {
        super( texture , width , height );

        this.x = x;
        this.y = y;
    }
}
