package jp.archilogic.docnext.android.coreview.image;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import jp.archilogic.docnext.android.info.SizeInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLUtils;

public class TextureInfo {
    static TextureInfo getBitmapInstance( final GL10 gl , final Resources res , final int resId ) {
        final Bitmap bitmap = BitmapFactory.decodeResource( res , resId );

        return new TextureInfo( gl , bitmap , bitmap.getWidth() , bitmap.getHeight() );
    }

    private static int getTextureSize( final int width , final int height ) {
        final int value = Math.max( width , height );

        for ( int size = 1 ; ; size <<= 1 ) {
            if ( size >= value ) {
                return size;
            }
        }
    }

    static TextureInfo getTiledBitmapInstance( final GL10 gl , final Resources res , final int resId ,
            final SizeInfo size ) {
        final Bitmap source = BitmapFactory.decodeResource( res , resId );

        final int len = getTextureSize( size.width , size.height );
        final Bitmap bitmap = Bitmap.createBitmap( len , len , Config.ARGB_8888 );

        final Canvas c = new Canvas( bitmap );
        final Paint p = new Paint();

        for ( int y = 0 ; y * source.getHeight() < size.height ; y++ ) {
            for ( int x = 0 ; x * source.getWidth() < size.width ; x++ ) {
                c.drawBitmap( source , x * source.getWidth() , y * source.getHeight() , p );
            }
        }

        source.recycle();

        return new TextureInfo( gl , bitmap , size.width , size.height );
    }

    int id;
    int width;
    int height;

    protected TextureInfo( final GL10 gl , final Bitmap bitmap , final int width , final int height ) {
        this( gl , width , height );

        bindTexture( gl , bitmap );
        bitmap.recycle();
    }

    protected TextureInfo( final GL10 gl , final int width , final int height ) {
        this.width = width;
        this.height = height;

        genTexture( gl );
    }

    /**
     * Crop is keeping left and top edge
     */
    void bindTexture( final GL10 gl , final Bitmap bitmap ) {
        gl.glBindTexture( GL10.GL_TEXTURE_2D , id );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MIN_FILTER , GL10.GL_LINEAR );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MAG_FILTER , GL10.GL_LINEAR );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_WRAP_S , GL10.GL_CLAMP_TO_EDGE );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_WRAP_T , GL10.GL_CLAMP_TO_EDGE );

        GLUtils.texImage2D( GL10.GL_TEXTURE_2D , 0 , bitmap , 0 );

        ( ( GL11 ) gl ).glTexParameteriv( GL10.GL_TEXTURE_2D , GL11Ext.GL_TEXTURE_CROP_RECT_OES , //
                new int[] { 0 , height , width , -height } , 0 );
    }

    protected void genTexture( final GL10 gl ) {
        final int[] texture = new int[ 1 ];

        gl.glGenTextures( 1 , texture , 0 );

        this.id = texture[ 0 ];
    }

}
