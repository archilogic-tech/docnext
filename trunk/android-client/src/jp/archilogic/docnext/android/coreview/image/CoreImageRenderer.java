package jp.archilogic.docnext.android.coreview.image;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import jp.archilogic.docnext.android.info.SizeInfo;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;

import com.google.common.collect.Lists;

public class CoreImageRenderer implements Renderer {
    private final Context _context;
    private PageInfo _page;
    private SizeInfo _surfaceSize;

    public CoreImageRenderer( final Context context ) {
        _context = context;
    }

    private PageInfo loadPage( final GL10 gl ) {
        try {
            final int width = 1024;
            final int height = 1479;
            final int TEXTURE_SIZE = 512;

            final PageInfo ret = new PageInfo();
            ret.width = width;
            ret.height = height;

            ret.textures = Lists.newArrayList();
            for ( int y = 0 ; y * TEXTURE_SIZE < height ; y++ ) {
                for ( int x = 0 ; x * TEXTURE_SIZE < width ; x++ ) {
                    final TextureInfo texture = new TextureInfo();
                    texture.x = x * TEXTURE_SIZE;
                    texture.y = y * TEXTURE_SIZE;
                    texture.width = Math.min( width - texture.x , TEXTURE_SIZE );
                    texture.height = Math.min( height - texture.y , TEXTURE_SIZE );

                    final InputStream in = _context.getAssets().open( String.format( "%d_%d.jpg" , x , y ) );
                    texture.texture = prepareTexture( gl , in , texture.width , texture.height );
                    IOUtils.closeQuietly( in );

                    ret.textures.add( texture );
                }
            }

            return ret;
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void onDrawFrame( final GL10 gl ) {
        gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

        for ( final TextureInfo texture : _page.textures ) {
            gl.glBindTexture( GL10.GL_TEXTURE_2D , texture.texture );
            ( ( GL11Ext ) gl ).glDrawTexfOES( texture.x , _surfaceSize.height - ( texture.y + texture.height ) , 0 ,
                    texture.width , texture.height );
        }
    }

    @Override
    public void onSurfaceChanged( final GL10 gl , final int width , final int height ) {
        gl.glEnable( GL10.GL_TEXTURE_2D );

        _surfaceSize = new SizeInfo( width , height );
    }

    @Override
    public void onSurfaceCreated( final GL10 gl , final EGLConfig config ) {
        _page = loadPage( gl );
    }

    /**
     * Crop is keeping left and top edge
     */
    private int prepareTexture( final GL10 gl , final InputStream in , final int cropWidth , final int cropHeight ) {
        final int[] texture = new int[ 1 ];

        gl.glGenTextures( 1 , texture , 0 );

        gl.glBindTexture( GL10.GL_TEXTURE_2D , texture[ 0 ] );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MIN_FILTER , GL10.GL_NEAREST );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MAG_FILTER , GL10.GL_LINEAR );

        final Bitmap bitmap = BitmapFactory.decodeStream( in );

        GLUtils.texImage2D( GL10.GL_TEXTURE_2D , 0 , bitmap , 0 );

        bitmap.recycle();

        ( ( GL11 ) gl ).glTexParameteriv( GL10.GL_TEXTURE_2D , GL11Ext.GL_TEXTURE_CROP_RECT_OES , //
                new int[] { 0 , cropHeight , cropWidth , -cropHeight } , 0 );

        return texture[ 0 ];
    }
}
