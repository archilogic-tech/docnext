package jp.archilogic.docnext.android.coreview.image;

public class PageTextureInfo extends TextureInfo {
    static PageTextureInfo getInstance( final int width , final int height , final int x , final int y ) {
        return new PageTextureInfo( width , height , x , y );
    }

    int x;
    int y;

    protected PageTextureInfo( final int width , final int height , final int x , final int y ) {
        super( width , height );

        this.x = x;
        this.y = y;
    }
}
