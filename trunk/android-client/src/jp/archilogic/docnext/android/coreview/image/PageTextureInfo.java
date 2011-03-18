package jp.archilogic.docnext.android.coreview.image;

public class PageTextureInfo extends TextureInfo {
    int x;
    int y;
    int px;
    int py;

    public PageTextureInfo( final int texture , final int width , final int height , final int x , final int y ,
            final int px , final int py ) {
        super( texture , width , height );

        this.x = x;
        this.y = y;
        this.px = px;
        this.py = py;
    }
}
