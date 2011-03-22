package jp.archilogic.docnext.android.coreview.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
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
    interface PageLoader {
        void load( int page );
    }

    private final Context _context;
    private final CoreImageEngine _engine = new CoreImageEngine();

    private TextureInfo _background;
    private PageInfo[] _pages;

    private final Queue< PageImageCache > _loadPageQueue = Lists.newLinkedList();
    private final ExecutorService _executor = Executors.newSingleThreadExecutor();

    int _fpsCounter = 0;
    long _fpsTime;

    private final PageLoader _loader = new PageLoader() {
        @Override
        public void load( final int page ) {
            _executor.execute( new Runnable() {
                @Override
                public void run() {
                    // 2 for waiting index update
                    if ( page < _engine.page - 2 || page > _engine.page + 2 ) {
                        return;
                    }

                    final PageImageCache cache = buildCache( page );

                    if ( page < _engine.page - 1 || page > _engine.page + 1 ) {
                        return;
                    }

                    _loadPageQueue.add( cache );
                }
            } );
        }
    };

    public CoreImageRenderer( final Context context ) {
        _context = context;
        _engine.setPageLoader( _loader );
    }

    void beginInteraction() {
        _engine.isInteracting = true;
    }

    private void bindPage( final GL10 gl , final PageImageCache cache ) {
        for ( int index = 0 ; index < cache.bitmaps.size() ; index++ ) {
            final Bitmap bitmap = cache.bitmaps.get( index );
            final PageTextureInfo texture = _pages[ cache.page % _pages.length ].textures.get( index );

            gl.glDeleteTextures( 1 , new int[] { texture.texture } , 0 );
            bindTexture( gl , texture , bitmap );

            bitmap.recycle();
        }

        _engine.loaded[ cache.page ] = true;
    }

    /**
     * Crop is keeping left and top edge
     */
    private void bindTexture( final GL10 gl , final TextureInfo texture , final Bitmap bitmap ) {
        gl.glBindTexture( GL10.GL_TEXTURE_2D , texture.texture );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MIN_FILTER , GL10.GL_LINEAR );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_MAG_FILTER , GL10.GL_LINEAR );

        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_WRAP_S , GL10.GL_CLAMP_TO_EDGE );
        gl.glTexParameterf( GL10.GL_TEXTURE_2D , GL10.GL_TEXTURE_WRAP_T , GL10.GL_CLAMP_TO_EDGE );

        GLUtils.texImage2D( GL10.GL_TEXTURE_2D , 0 , bitmap , 0 );

        ( ( GL11 ) gl ).glTexParameteriv( GL10.GL_TEXTURE_2D , GL11Ext.GL_TEXTURE_CROP_RECT_OES , //
                new int[] { 0 , texture.height , texture.width , -texture.height } , 0 );
    }

    private PageImageCache buildCache( final int page ) {
        try {
            final PageImageCache ret = new PageImageCache( page );

            for ( final PageTextureInfo texture : _pages[ page % _pages.length ].textures ) {
                final InputStream in =
                        new FileInputStream( Kernel.getLocalProvider().getImagePath( _engine.id , page , 0 ,
                                texture.px , texture.py ) );

                ret.bitmaps.add( BitmapFactory.decodeStream( in ) );

                IOUtils.closeQuietly( in );
            }

            return ret;
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    void doubleTap( final PointF point ) {
        _engine.doubleTap( point );
    }

    void drag( final PointF delta ) {
        _engine.drag( delta );
    }

    void endInteraction() {
        _engine.isInteracting = false;
    }

    private int genTexture( final GL10 gl ) {
        final int[] texture = new int[ 1 ];

        gl.glGenTextures( 1 , texture , 0 );

        return texture[ 0 ];
    }

    CoreImageDirection getDirection() {
        return _engine.direction;
    }

    @Override
    public void onDrawFrame( final GL10 gl ) {
        if ( _fpsCounter == 0 ) {
            _fpsTime = SystemClock.elapsedRealtime();
        }

        while ( !_loadPageQueue.isEmpty() ) {
            bindPage( gl , _loadPageQueue.poll() );
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

        final float hPad = _engine.getHorizontalPadding();
        final float vPad = _engine.getVerticalPadding();
        final int xSign = _engine.direction.toXSign();
        final int ySign = _engine.direction.toYSign();

        for ( int delta = -1 ; delta <= 1 ; delta++ ) {
            final int page = _engine.page + delta;

            if ( page >= 0 && page < _engine.loaded.length && _engine.loaded[ page ] ) {
                for ( final PageTextureInfo tex : _pages[ page % _pages.length ].textures ) {
                    // manual clipping seems no effect, so draw all texture

                    gl.glBindTexture( GL10.GL_TEXTURE_2D , tex.texture );

                    final float x =
                            _engine.matrix.x( tex.x ) + hPad + _engine.matrix.length( _engine.pageSize.width ) * delta
                                    * xSign;
                    final float y =
                            _engine.surfaceSize.height
                                    - ( _engine.matrix.y( tex.y + tex.height ) + vPad + _engine.matrix
                                            .length( _engine.pageSize.height ) * delta * ySign );
                    final float w = _engine.matrix.length( tex.width );
                    final float h = _engine.matrix.length( tex.height );

                    ( ( GL11Ext ) gl ).glDrawTexfOES( x , y , 0 , w , h );
                }
            }
        }

        _fpsCounter++;
        if ( _fpsCounter == 300 ) {
            _fpsCounter = 0;
            System.err.println( "FPS: " + 300.0 * 1000 / ( SystemClock.elapsedRealtime() - _fpsTime ) );
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
        _background = new TextureInfo( genTexture( gl ) , 256 , 256 );

        final InputStream in = _context.getResources().openRawResource( R.drawable.background );
        final Bitmap bitmap = BitmapFactory.decodeStream( in );
        bindTexture( gl , _background , bitmap );
        bitmap.recycle();
        IOUtils.closeQuietly( in );

        final DocInfo doc = Kernel.getLocalProvider().getDocInfo( _engine.id );
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( _engine.id );

        _pages = new PageInfo[] { preparePageTextureHolder( gl , image.width , image.height ) , //
                preparePageTextureHolder( gl , image.width , image.height ) , //
                preparePageTextureHolder( gl , image.width , image.height ) };

        _engine.pageSize = new SizeInfo( image.width , image.height );
        _engine.page = 0;
        _engine.loaded = new boolean[ doc.pages ];

        bindPage( gl , buildCache( 0 ) );
        bindPage( gl , buildCache( 1 ) );
    }

    private PageInfo preparePageTextureHolder( final GL10 gl , final int width , final int height ) {
        final int TEXTURE_SIZE = 512;

        final PageInfo ret = new PageInfo();

        ret.textures = Lists.newArrayList();
        for ( int py = 0 ; py * TEXTURE_SIZE < height ; py++ ) {
            for ( int px = 0 ; px * TEXTURE_SIZE < width ; px++ ) {
                final int x = px * TEXTURE_SIZE;
                final int y = py * TEXTURE_SIZE;

                ret.textures.add( new PageTextureInfo( genTexture( gl ) , Math.min( width - x , TEXTURE_SIZE ) ,//
                        Math.min( height - y , TEXTURE_SIZE ) , x , y , px , py ) );
            }
        }

        return ret;
    }

    void setDirection( final CoreImageDirection direction ) {
        _engine.direction = direction;
    }

    void setId( final long id ) {
        _engine.id = id;
    }

    void zoom( final float scaleDelta , final PointF center ) {
        _engine.zoom( scaleDelta , center );
    }
}
