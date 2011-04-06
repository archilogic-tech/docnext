package jp.archilogic.docnext.android.coreview.image;

import javax.microedition.khronos.opengles.GL10;

public class PageTextureInfo extends TextureInfo {
    static PageTextureInfo getInstance( final GL10 gl , final int width , final int height , final int x , final int y ) {
        return new PageTextureInfo( gl , width , height , x , y );
    }

    int x;
    int y;

    protected PageTextureInfo( final GL10 gl , final int width , final int height , final int x , final int y ) {
        super( gl , width , height );

        this.x = x;
        this.y = y;
    }
}
