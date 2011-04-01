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
import jp.archilogic.docnext.android.coreview.image.CoreImageEngine.OnScaleChangeListener;
import jp.archilogic.docnext.android.coreview.image.PageInfo.PageTextureStatus;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.info.SizeFInfo;
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

        void unload( int page );
    }

    private static final boolean DEBUG = false;

    private final Context _context;
    private final CoreImageEngine _engine = new CoreImageEngine();

    private TextureInfo _background;
    private TextureInfo _border;
    private TextureInfo _blank;
    private PageInfo[] _pages;

    private final Queue< PageImageCache > _loadQueue = Lists.newLinkedList();
    private final Queue< PageImageCache > _unloadQueue = Lists.newLinkedList();
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

                    final PageTextureInfo[][] textures = _pages[ page ].textures[ 0 ];
                    for ( int py = 0 ; py < textures.length ; py++ ) {
                        for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                            _loadQueue.add( buildCache( page , 0 , px , py ) );

                            if ( page < _engine.page - 2 || page > _engine.page + 2 ) {
                                unload( page );
                                return;
                            }
                        }
                    }
                }
            } );
        }

        @Override
        public void unload( final int page ) {
            for ( int level = 0 ; level < _engine.nLevel ; level++ ) {
                final PageTextureInfo[][] textures = _pages[ page ].textures[ level ];
                for ( int py = 0 ; py < textures.length ; py++ ) {
                    for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                        // XXX consider to delete _loadQueue (load is x10 heavy than unload)
                        _unloadQueue.add( new PageImageCache( page , level , px , py , null ) );
                    }
                }
            }
        }
    };

    public CoreImageRenderer( final Context context ) {
        _context = context;
        _engine.setPageLoader( _loader );
    }

    void beginInteraction() {
        _engine.isInteracting = true;
    }

    private void bindPageImage( final GL10 gl , final PageImageCache cache ) {
        final PageTextureInfo texture = _pages[ cache.page ].textures[ cache.level ][ cache.py ][ cache.px ];

        bindTexture( gl , texture , cache.bitmap );

        _pages[ cache.page ].statuses[ cache.level ][ cache.py ][ cache.px ] = PageTextureStatus.BIND;
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

    private void buildAndBindPage( final GL10 gl , final int page ) {
        if ( page < 0 || page >= _engine.pages ) {
            return;
        }

        final PageTextureInfo[][] texture = _pages[ page ].textures[ 0 ];
        for ( int py = 0 ; py < texture.length ; py++ ) {
            for ( int px = 0 ; px < texture[ py ].length ; px++ ) {
                bindPageImage( gl , buildCache( page , 0 , px , py ) );
            }
        }
    }

    private PageImageCache buildCache( final int page , final int level , final int px , final int py ) {
        InputStream in = null;
        try {
            in = new FileInputStream( Kernel.getLocalProvider().getImagePath( _engine.id , page , level , px , py ) );

            return new PageImageCache( page , level , px , py , BitmapFactory.decodeStream( in ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    void checkAndDrawSingleImage( final GL10 gl , final CoreImageMatrix matrix , final SizeFInfo padding ,
            final int xSign , final int ySign , final int level , final float factor , final int delta ,
            final int page , final PageTextureInfo[][] textures , final PageTextureStatus[][] statuses , final int py ,
            final int px , final PageTextureInfo tex ) {
        final float x =
                matrix.x( tex.x / factor ) + padding.width + matrix.length( _engine.pageSize.width ) * delta * xSign;
        final float y =
                _engine.surfaceSize.height
                        - ( matrix.y( ( tex.y + tex.height ) / factor ) + padding.height + matrix
                                .length( _engine.pageSize.height ) * delta * ySign );
        final float w = matrix.length( tex.width ) / factor;
        final float h = matrix.length( tex.height ) / factor;

        if ( level == 0 ) {
            if ( statuses[ py ][ px ] == PageTextureStatus.BIND ) {
                // manual clipping seems no effect, so draw all texture
                drawSingleImage( gl , textures[ py ][ px ].texture , x , y , w , h );
            } else {
                drawSingleImage( gl , _blank.texture , x , y , w , h );
            }
        } else {
            final boolean isNeeded = ( level == 0 || matrix.scale >= Math.pow( 2 , level - 1 ) ) && //
                    x + w >= 0 && x < _engine.surfaceSize.width && //
                    y + h >= 0 && y < _engine.surfaceSize.height;

            if ( isNeeded ) {
                if ( statuses[ py ][ px ] == PageTextureStatus.BIND ) {
                    drawSingleImage( gl , textures[ py ][ px ].texture , x , y , w , h );
                } else if ( statuses[ py ][ px ] == PageTextureStatus.UNBIND ) {
                    requestTexture( page , level , px , py );
                }
            } else {
                if ( statuses[ py ][ px ] == PageTextureStatus.BIND ) {
                    _pages[ page ].statuses[ level ][ py ][ px ] = PageTextureStatus.UNBIND;
                    gl.glDeleteTextures( 1 , new int[] { _pages[ page ].textures[ level ][ py ][ px ].texture } , 0 );
                }
            }
        }
    }

    void doubleTap( final PointF point ) {
        _engine.doubleTap( point );
    }

    void drag( final PointF delta ) {
        _engine.drag( delta );
    }

    private void drawBackground( final GL10 gl ) {
        gl.glBindTexture( GL10.GL_TEXTURE_2D , _background.texture );
        for ( int y = 0 ; y * _background.height < _engine.surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * _background.width < _engine.surfaceSize.width ; x++ ) {
                ( ( GL11Ext ) gl ).glDrawTexfOES( x * _background.width , y * _background.height , 0 ,
                        _background.width , _background.height );
            }
        }
    }

    private void drawImage( final GL10 gl , final CoreImageMatrix matrix , final SizeFInfo padding ) {
        final int xSign = _engine.direction.toXSign();
        final int ySign = _engine.direction.toYSign();

        for ( int level = 0 ; level < _engine.nLevel ; level++ ) {
            final float factor = ( float ) Math.pow( 2 , level );

            for ( int delta = -1 ; delta <= 1 ; delta++ ) {
                final int page = _engine.page + delta;

                if ( page >= 0 && page < _pages.length ) {
                    final PageTextureInfo[][] textures = _pages[ page ].textures[ level ];
                    final PageTextureStatus[][] statuses = _pages[ page ].statuses[ level ];

                    for ( int py = 0 ; py < textures.length ; py++ ) {
                        for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                            checkAndDrawSingleImage( gl , matrix , padding , xSign , ySign , level , factor , delta ,
                                    page , textures , statuses , py , px , textures[ py ][ px ] );
                        }
                    }
                }
            }
        }
    }

    private void drawSingleImage( final GL10 gl , final int texture , final float x , final float y , final float w ,
            final float h ) {
        gl.glBindTexture( GL10.GL_TEXTURE_2D , texture );
        ( ( GL11Ext ) gl ).glDrawTexfOES( x , y , 0 , w , h );

        if ( DEBUG ) {
            final int BORDER_WIDTH = 1;
            gl.glBindTexture( GL10.GL_TEXTURE_2D , _border.texture );
            ( ( GL11Ext ) gl ).glDrawTexfOES( x , y , 0 , w , BORDER_WIDTH );
            ( ( GL11Ext ) gl ).glDrawTexfOES( x + w - BORDER_WIDTH , y , 0 , BORDER_WIDTH , h );
            ( ( GL11Ext ) gl ).glDrawTexfOES( x , y + h - BORDER_WIDTH , 0 , w , BORDER_WIDTH );
            ( ( GL11Ext ) gl ).glDrawTexfOES( x , y , 0 , BORDER_WIDTH , h );
        }
    }

    void endInteraction() {
        _engine.isInteracting = false;
    }

    private int genTexture( final GL10 gl ) {
        final int[] texture = new int[ 1 ];

        gl.glGenTextures( 1 , texture , 0 );

        return texture[ 0 ];
    }

    int getCurrentPage() {
        return _engine.getCurrentPage();
    }

    CoreImageDirection getDirection() {
        return _engine.direction;
    }

    @Override
    public void onDrawFrame( final GL10 gl ) {
        if ( _fpsCounter == 0 ) {
            _fpsTime = SystemClock.elapsedRealtime();
        }

        while ( !_loadQueue.isEmpty() ) {
            bindPageImage( gl , _loadQueue.poll() );
        }

        while ( !_unloadQueue.isEmpty() ) {
            unbindPageImage( gl , _unloadQueue.poll() );
        }

        _engine.update();

        // copy for thread consistency
        final CoreImageMatrix matrix = new CoreImageMatrix( _engine.matrix );
        final SizeFInfo padding = new SizeFInfo( _engine.getHorizontalPadding() , _engine.getVerticalPadding() );

        gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

        drawBackground( gl );
        drawImage( gl , matrix , padding );

        final int BORDER_WIDTH = 1;
        gl.glBindTexture( GL10.GL_TEXTURE_2D , _border.texture );
        ( ( GL11Ext ) gl ).glDrawTexfOES( 0 , _engine.surfaceSize.height / 2 , 0 , _engine.surfaceSize.width ,
                BORDER_WIDTH );
        ( ( GL11Ext ) gl ).glDrawTexfOES( _engine.surfaceSize.width / 2 , 0 , 0 , BORDER_WIDTH ,
                _engine.surfaceSize.height );

        _fpsCounter++;
        if ( _fpsCounter == 300 ) {
            _fpsCounter = 0;
            System.err.println( "FPS: " + 300.0 * 1000 / ( SystemClock.elapsedRealtime() - _fpsTime ) );
        }
    }

    @Override
    public void onSurfaceChanged( final GL10 gl , final int width , final int height ) {
        gl.glEnable( GL10.GL_TEXTURE_2D );
        gl.glEnable( GL10.GL_BLEND );
        gl.glBlendFunc( GL10.GL_SRC_ALPHA , GL10.GL_ONE_MINUS_SRC_ALPHA );

        _engine.surfaceSize = new SizeInfo( width , height );
        _engine.initScale();
    }

    @Override
    public void onSurfaceCreated( final GL10 gl , final EGLConfig config ) {
        System.err.println( "*** onSurfaceCreated" );

        _background = prepareTexture( gl , R.drawable.background );
        _border = prepareTexture( gl , R.drawable.border );
        _blank = prepareTexture( gl , R.drawable.blank );

        final DocInfo doc = Kernel.getLocalProvider().getDocInfo( _engine.id );
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( _engine.id );
        _engine.nLevel = image.nLevel;

        _pages = new PageInfo[ doc.pages ];
        for ( int page = 0 ; page < _pages.length ; page++ ) {
            _pages[ page ] = preparePageTextureHolder( gl , image );
        }

        _engine.pageSize = new SizeInfo( image.width , image.height );
        // _engine.page = 0;
        _engine.pages = doc.pages;

        buildAndBindPage( gl , _engine.page - 1 );
        buildAndBindPage( gl , _engine.page );
        buildAndBindPage( gl , _engine.page + 1 );
    }

    private PageInfo preparePageTextureHolder( final GL10 gl , final ImageInfo image ) {
        final int TEXTURE_SIZE = 512;

        final PageInfo ret = new PageInfo();

        ret.textures = new PageTextureInfo[ _engine.nLevel ][][];
        ret.statuses = new PageTextureStatus[ _engine.nLevel ][][];
        for ( int level = 0 ; level < _engine.nLevel ; level++ ) {
            final int factor = ( int ) Math.pow( 2 , level );
            final int nx = ( image.width * factor - 1 ) / TEXTURE_SIZE + 1;
            final int ny = ( image.height * factor - 1 ) / TEXTURE_SIZE + 1;
            ret.textures[ level ] = new PageTextureInfo[ ny ][ nx ];
            ret.statuses[ level ] = new PageTextureStatus[ ny ][ nx ];

            for ( int py = 0 ; py < ny ; py++ ) {
                for ( int px = 0 ; px < nx ; px++ ) {
                    final int x = px * TEXTURE_SIZE;
                    final int y = py * TEXTURE_SIZE;

                    ret.textures[ level ][ py ][ px ] =
                            new PageTextureInfo( genTexture( gl ) ,
                                    Math.min( image.width * factor - x , TEXTURE_SIZE ) , Math.min( image.height
                                            * factor - y , TEXTURE_SIZE ) , x , y );
                    ret.statuses[ level ][ py ][ px ] = PageTextureStatus.UNBIND;
                }
            }
        }

        return ret;
    }

    private TextureInfo prepareTexture( final GL10 gl , final int resId ) {
        final InputStream in = _context.getResources().openRawResource( resId );
        final Bitmap bitmap = BitmapFactory.decodeStream( in );

        final TextureInfo ret = new TextureInfo( genTexture( gl ) , bitmap.getWidth() , bitmap.getHeight() );

        bindTexture( gl , ret , bitmap );

        bitmap.recycle();
        IOUtils.closeQuietly( in );

        return ret;
    }

    private void requestTexture( final int page , final int level , final int px , final int py ) {
        _pages[ page ].statuses[ level ][ py ][ px ] = PageTextureStatus.LOAD;
        _executor.execute( new Runnable() {
            @Override
            public void run() {
                // 2 for waiting index update
                if ( page < _engine.page - 2 || page > _engine.page + 2 ) {
                    return;
                }

                _loadQueue.add( buildCache( page , level , px , py ) );
            }
        } );
    }

    void setDirection( final CoreImageDirection direction ) {
        _engine.direction = direction;
    }

    void setId( final long id ) {
        _engine.id = id;
    }

    void setOnScaleChangeListener( final OnScaleChangeListener l ) {
        _engine.setOnScaleChangeListener( l );
    }

    void setPage( final int page ) {
        _engine.page = page;
    }

    private void unbindPageImage( final GL10 gl , final PageImageCache cache ) {
        _pages[ cache.page ].statuses[ cache.level ][ cache.py ][ cache.px ] = PageTextureStatus.UNBIND;

        gl.glDeleteTextures( 1 ,
                new int[] { _pages[ cache.page ].textures[ cache.level ][ cache.py ][ cache.px ].texture } , 0 );
    }

    void zoom( final float scaleDelta , final PointF center ) {
        _engine.zoom( scaleDelta , center );
    }

    void zoomByLevel( final int delta ) {
        _engine.zoomByLevel( delta );
    }
}
