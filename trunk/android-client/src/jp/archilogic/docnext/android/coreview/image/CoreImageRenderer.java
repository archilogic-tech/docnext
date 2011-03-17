package jp.archilogic.docnext.android.coreview.image;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.info.SizeInfo;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.SystemClock;

import com.google.common.collect.Lists;

/**
 * Handle OpenGL features
 */
public class CoreImageRenderer implements Renderer {
    private final Context _context;
    private final CoreImageEngine _engine = new CoreImageEngine();

    private TextureInfo _background;
    private PageInfo[] _pages;

    int _fpsCounter = 0;
    long _fpsTime;

    public CoreImageRenderer( final Context context ) {
        _context = context;
    }

    void beginInteraction() {
        _engine.isInteracting = true;
    }

    void drag( final PointF delta ) {
        _engine.drag( delta );
    }

    void endInteraction() {
        _engine.isInteracting = false;
    }

    private PageInfo loadPage( final GL10 gl , final int page ) {
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
                    final int tx = x * TEXTURE_SIZE;
                    final int ty = y * TEXTURE_SIZE;
                    final int tw = Math.min( width - tx , TEXTURE_SIZE );
                    final int th = Math.min( height - ty , TEXTURE_SIZE );

                    final InputStream in = _context.getAssets().open( String.format( "%d_%d_%d.jpg" , page , x , y ) );
                    final int tt = prepareTexture( gl , in , tw , th ).texture;
                    IOUtils.closeQuietly( in );

                    ret.textures.add( new PageTextureInfo( tt , tw , th , tx , ty ) );
                }
            }

            return ret;
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void onDrawFrame( final GL10 gl ) {
        if ( _fpsCounter == 0 ) {
            _fpsTime = SystemClock.elapsedRealtime();
        }

        _engine.update();

        gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

        gl.glBindTexture( GL10.GL_TEXTURE_2D , _background.texture );
        for ( int y = 0 ; y * _background.height < _engine.surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.width < _engine.surfaceSize.width ; x++ ) {
                ( ( GL11Ext ) gl ).glDrawTexfOES( x * _background.width , y * _background.height , 0 ,
                        _background.width , _background.height );
            }
        }

        for ( int index = 0 ; index < 3 ; index++ ) {
            if ( _pages[ index ] != null ) {
                for ( final PageTextureInfo texture : _pages[ index ].textures ) {
                    gl.glBindTexture( GL10.GL_TEXTURE_2D , texture.texture );

                    final float x =
                            _engine.matrix.x( texture.x ) + _engine.getHorizontalPadding()
                                    + _engine.matrix.length( _engine.pageSize.width ) * ( index - 1 )
                                    * _engine.direction.toXSign();
                    final float y =
                            _engine.surfaceSize.height - _engine.matrix.y( texture.y + texture.height )
                                    - _engine.getVerticalPadding() + _engine.matrix.length( _engine.pageSize.height )
                                    * ( index - 1 ) * _engine.direction.toYSign();
                    final float w = _engine.matrix.length( texture.width );
                    final float h = _engine.matrix.length( texture.height );

                    ( ( GL11Ext ) gl ).glDrawTexfOES( x , y , 0 , w , h );
                }
            }
        }

        _fpsCounter++;
        if ( _fpsCounter == 180 ) {
            _fpsCounter = 0;
            System.err.println( "FPS: " + 180.0 * 1000 / ( SystemClock.elapsedRealtime() - _fpsTime ) );
        }
    }

    @Override
    public void onSurfaceChanged( final GL10 gl , final int width , final int height ) {
        gl.glEnable( GL10.GL_TEXTURE_2D );

        _engine.surfaceSize = new SizeInfo( width , height );
        _engine.initScale();
    }

    @Override
    public void onSurfaceCreated( final GL10 gl , final EGLConfig config ) {
        final InputStream in = _context.getResources().openRawResource( R.drawable.background );
        _background = prepareTexture( gl , in , 256 , 256 );
        IOUtils.closeQuietly( in );

        _pages = new PageInfo[] { loadPage( gl , 6 ) , loadPage( gl , 7 ) , loadPage( gl , 8 ) };

        _engine.pageSize = new SizeInfo( _pages[ 0 ].width , _pages[ 0 ].height );
        _engine.direction = CoreImageDirection.R2L;
    }

    /**
     * Crop is keeping left and top edge
     */
    private TextureInfo prepareTexture( final GL10 gl , final InputStream in , final int cropWidth ,
            final int cropHeight ) {
        final int[] texture = new int[ 1 ];

        gl.glGenTextures( 1 , texture , 0 );

        gl.glBindTexture( GL10.GL_TEXTURE_2D , texture[ 0 ] );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MIN_FILTER , GL10.GL_LINEAR );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MAG_FILTER , GL10.GL_LINEAR );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_WRAP_S , GL10.GL_CLAMP_TO_EDGE );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_WRAP_T , GL10.GL_CLAMP_TO_EDGE );

        final Bitmap bitmap = BitmapFactory.decodeStream( in );

        GLUtils.texImage2D( GL10.GL_TEXTURE_2D , 0 , bitmap , 0 );

        bitmap.recycle();

        ( ( GL11 ) gl ).glTexParameteriv( GL10.GL_TEXTURE_2D , GL11Ext.GL_TEXTURE_CROP_RECT_OES , //
                new int[] { 0 , cropHeight , cropWidth , -cropHeight } , 0 );

        return new TextureInfo( texture[ 0 ] , cropWidth , cropHeight );
    }

    void zoom( final float scaleDelta , final PointF center ) {
        _engine.zoom( scaleDelta , center );
    }
}
