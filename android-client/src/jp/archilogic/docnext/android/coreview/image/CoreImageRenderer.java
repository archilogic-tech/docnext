package jp.archilogic.docnext.android.coreview.image;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.SystemClock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Handle OpenGL features
 */
public class CoreImageRenderer implements Renderer {
    interface PageLoader {
        void load( int page );

        void unload( int page );
    }

    private static final int TEXTURE_SIZE = 512;

    private final Context _context;
    private final CoreImageEngine _engine = new CoreImageEngine();

    private TextureInfo _background;
    private TextureInfo _blank;
    private PageInfo[] _pages;

    private final Queue< LoadBitmapTask > _bindQueue = Lists.newLinkedList();
    private final Queue< LoadBitmapTask > _unbindQueue = Lists.newLinkedList();
    private final ExecutorService _executor = new ThreadPoolExecutor( 1 , 1 , 0L , TimeUnit.MILLISECONDS ,
            new PriorityBlockingQueue< Runnable >( 11 , new Comparator< Runnable >() {
                @Override
                public int compare( final Runnable o1 , final Runnable o2 ) {
                    // bigger value is first
                    return Integer.valueOf( ( ( HasPriority ) o2 ).getPriority() ).compareTo(
                            Integer.valueOf( ( ( HasPriority ) o1 ).getPriority() ) );
                }
            } ) );
    private final Map< Integer , List< LoadBitmapTask > > _tasks = Maps.newHashMap();

    // use for rendering, avoid GC
    private final CoreImageMatrix _immutableMatrix = new CoreImageMatrix();
    private final SizeFInfo _immutablePadding = new SizeFInfo( 0 , 0 );

    int _fpsCounter = 0;
    long _fpsTime;
    long _frameSum;

    private final PageLoader _loader = new PageLoader() {
        @Override
        public void load( final int page ) {
            if ( page < 0 || page >= _engine.pages ) {
                return;
            }

            if ( _tasks.get( page ) == null ) {
                _tasks.put( page , Lists.< LoadBitmapTask > newArrayList() );
            }

            for ( int level = 0 ; level < _engine.nLevel ; level++ ) {
                final PageTextureInfo[][] textures = _pages[ page ].textures[ level ];
                for ( int py = 0 ; py < textures.length ; py++ ) {
                    for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                        final LoadBitmapTask task =
                                new LoadBitmapTask( _engine , page , level , px , py , _tasks , _bindQueue );

                        _tasks.get( page ).add( task );
                        _executor.execute( task );
                    }
                }
            }
        }

        @Override
        public void unload( final int page ) {
            if ( _tasks.get( page ) != null ) {
                for ( final LoadBitmapTask task : _tasks.get( page ) ) {
                    task.cancel();
                }

                _tasks.remove( page );
            }

            // unbind all
            for ( int level = 0 ; level < _engine.nLevel ; level++ ) {
                final PageTextureInfo[][] textures = _pages[ page ].textures[ level ];
                for ( int py = 0 ; py < textures.length ; py++ ) {
                    for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                        _unbindQueue.add( new LoadBitmapTask( null , page , level , px , py , null , null ) );
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

    private void bindPageImage( final GL10 gl , final LoadBitmapTask task ) {
        final PageTextureInfo texture = _pages[ task.page ].textures[ task.level ][ task.py ][ task.px ];

        bindTexture( gl , texture , task.bitmap );

        task.bitmap.recycle();

        _pages[ task.page ].statuses[ task.level ][ task.py ][ task.px ] = PageTextureStatus.BIND;
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

    private void checkAndDrawSingleImage( final GL10 gl , final CoreImageMatrix matrix , final int level ,
            final PageTextureInfo[][] textures , final PageTextureStatus[][] statuses , final int py , final int px ,
            final float x , final float y , final float w , final float h ) {
        final boolean isVisible =
                x + w >= 0 && x < _engine.surfaceSize.width && y + h >= 0 && y < _engine.surfaceSize.height;

        if ( isVisible && ( level == 0 || matrix.scale >= Math.pow( 2 , level - 1 ) ) ) {
            if ( statuses[ py ][ px ] == PageTextureStatus.BIND ) {
                drawSingleImage( gl , textures[ py ][ px ].texture , x , y , w , h );
            } else if ( level == 0 ) {
                drawSingleImage( gl , _blank.texture , x , y , w , h );
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
        ( ( GL11Ext ) gl ).glDrawTexfOES( 0 , 0 , 0 , _background.width , _background.height );
    }

    private void drawImage( final GL10 gl , final CoreImageMatrix matrix , final SizeFInfo padding ) {
        final int xSign = _engine.direction.toXSign();
        final int ySign = _engine.direction.toYSign();

        for ( int level = 0 ; level < _engine.nLevel ; level++ ) {
            final float factor = ( float ) Math.pow( 2 , level );

            final float size = matrix.length( TEXTURE_SIZE ) / factor;

            for ( int delta = -1 ; delta <= 1 ; delta++ ) {
                final int page = _engine.page + delta;

                if ( page >= 0 && page < _pages.length ) {
                    final PageTextureInfo[][] textures = _pages[ page ].textures[ level ];
                    final PageTextureStatus[][] statuses = _pages[ page ].statuses[ level ];

                    float y =
                            _engine.surfaceSize.height
                                    - ( matrix.y( TEXTURE_SIZE / factor ) + padding.height + matrix
                                            .length( _engine.pageSize.height ) * delta * ySign );
                    float height = textures.length > 1 ? size : matrix.length( textures[ 0 ][ 0 ].height ) / factor;

                    for ( int py = 0 ; py < textures.length ; py++ ) {
                        float x =
                                matrix.x( 0 ) + padding.width + matrix.length( _engine.pageSize.width ) * delta * xSign;

                        for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                            checkAndDrawSingleImage( gl , matrix , level , textures , statuses , py , px , x , y ,
                                    size , height );

                            x += size;
                        }

                        if ( py + 1 == textures.length - 1 ) {
                            height = matrix.length( textures[ py + 1 ][ 0 ].height ) / factor;
                        }

                        y -= height;
                    }
                }
            }
        }
    }

    private void drawSingleImage( final GL10 gl , final int texture , final float x , final float y , final float w ,
            final float h ) {
        gl.glBindTexture( GL10.GL_TEXTURE_2D , texture );
        ( ( GL11Ext ) gl ).glDrawTexfOES( x , y , 0 , w , h );
    }

    void endInteraction() {
        _engine.isInteracting = false;
    }

    void fling( final PointF velocity ) {
        _engine.fling( velocity );
    }

    private int genTexture( final GL10 gl ) {
        final int[] texture = new int[ 1 ];

        gl.glGenTextures( 1 , texture , 0 );

        return texture[ 0 ];
    }

    int getCurrentPage() {
        return _engine.page;
    }

    CoreImageDirection getDirection() {
        return _engine.direction;
    }

    @Override
    public void onDrawFrame( final GL10 gl ) {
        final long t = SystemClock.elapsedRealtime();

        while ( !_bindQueue.isEmpty() ) {
            bindPageImage( gl , _bindQueue.poll() );
        }

        while ( !_unbindQueue.isEmpty() ) {
            unbindPageImage( gl , _unbindQueue.poll() );
        }

        _engine.update();

        // copy for thread consistency
        _immutableMatrix.scale = _engine.matrix.scale;
        _immutableMatrix.tx = _engine.matrix.tx;
        _immutableMatrix.ty = _engine.matrix.ty;
        _immutablePadding.width = _engine.getHorizontalPadding();
        _immutablePadding.height = _engine.getVerticalPadding();

        drawBackground( gl );
        drawImage( gl , _immutableMatrix , _immutablePadding );

        _fpsCounter++;
        _frameSum += SystemClock.elapsedRealtime() - t;
        if ( _fpsCounter == 120 ) {
            System.err.println( "FPS: " + 120.0 * 1000 / ( SystemClock.elapsedRealtime() - _fpsTime ) + ", avg: "
                    + _frameSum / 120.0 );

            _fpsTime = SystemClock.elapsedRealtime();
            _fpsCounter = 0;
            _frameSum = 0;
        }
    }

    @Override
    public void onSurfaceChanged( final GL10 gl , final int width , final int height ) {
        gl.glEnable( GL10.GL_TEXTURE_2D );
        gl.glEnable( GL10.GL_BLEND );
        gl.glBlendFunc( GL10.GL_SRC_ALPHA , GL10.GL_ONE_MINUS_SRC_ALPHA );

        _engine.surfaceSize = new SizeInfo( width , height );
        _engine.initScale();

        _background = prepareBackgroundTexture( gl , _context.getResources() );

        final int[] caps =
                { GL11.GL_ALPHA_TEST , GL11.GL_BLEND , GL11.GL_CLIP_PLANE0 , GL11.GL_CLIP_PLANE1 , GL11.GL_CLIP_PLANE2 ,
                        GL11.GL_CLIP_PLANE3 , GL11.GL_CLIP_PLANE4 , GL11.GL_CLIP_PLANE5 , GL11.GL_COLOR_LOGIC_OP ,
                        GL11.GL_COLOR_MATERIAL , GL11.GL_CULL_FACE , GL11.GL_DEPTH_TEST , GL11.GL_DITHER , GL11.GL_FOG ,
                        GL11.GL_LIGHT0 , GL11.GL_LIGHT1 , GL11.GL_LIGHT2 , GL11.GL_LIGHT3 , GL11.GL_LIGHT4 ,
                        GL11.GL_LIGHT5 , GL11.GL_LIGHT6 , GL11.GL_LIGHT7 , GL11.GL_LIGHTING , GL11.GL_LINE_SMOOTH ,
                        GL11.GL_MULTISAMPLE , GL11.GL_NORMALIZE , GL11.GL_POINT_SMOOTH , GL11.GL_POLYGON_OFFSET_FILL ,
                        GL11.GL_RESCALE_NORMAL , GL11.GL_SAMPLE_ALPHA_TO_COVERAGE , GL11.GL_SAMPLE_ALPHA_TO_ONE ,
                        GL11.GL_SAMPLE_COVERAGE , GL11.GL_SCISSOR_TEST , GL11.GL_STENCIL_TEST , GL11.GL_TEXTURE_2D };
        final String[] names =
                { "GL11.GL_ALPHA_TEST" , "GL11.GL_BLEND" , "GL11.GL_CLIP_PLANE0" , "GL11.GL_CLIP_PLANE1" ,
                        "GL11.GL_CLIP_PLANE2" , "GL11.GL_CLIP_PLANE3" , "GL11.GL_CLIP_PLANE4" , "GL11.GL_CLIP_PLANE5" ,
                        "GL11.GL_COLOR_LOGIC_OP" , "GL11.GL_COLOR_MATERIAL" , "GL11.GL_CULL_FACE" ,
                        "GL11.GL_DEPTH_TEST" , "GL11.GL_DITHER" , "GL11.GL_FOG" , "GL11.GL_LIGHT0" , "GL11.GL_LIGHT1" ,
                        "GL11.GL_LIGHT2" , "GL11.GL_LIGHT3" , "GL11.GL_LIGHT4" , "GL11.GL_LIGHT5" , "GL11.GL_LIGHT6" ,
                        "GL11.GL_LIGHT7" , "GL11.GL_LIGHTING" , "GL11.GL_LINE_SMOOTH" , "GL11.GL_MULTISAMPLE" ,
                        "GL11.GL_NORMALIZE" , "GL11.GL_POINT_SMOOTH" , "GL11.GL_POLYGON_OFFSET_FILL" ,
                        "GL11.GL_RESCALE_NORMAL" , "GL11.GL_SAMPLE_ALPHA_TO_COVERAGE" , "GL11.GL_SAMPLE_ALPHA_TO_ONE" ,
                        "GL11.GL_SAMPLE_COVERAGE" , "GL11.GL_SCISSOR_TEST" , "GL11.GL_STENCIL_TEST" ,
                        "GL11.GL_TEXTURE_2D" };

        for ( int index = 0 ; index < caps.length ; index++ ) {
            System.err.println( names[ index ] + ": " + ( ( GL11 ) gl ).glIsEnabled( caps[ index ] ) );
        }
    }

    @Override
    public void onSurfaceCreated( final GL10 gl , final EGLConfig config ) {
        _blank = prepareTexture( gl , _context.getResources() , R.drawable.blank );

        final DocInfo doc = Kernel.getLocalProvider().getDocInfo( _engine.id );
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( _engine.id );
        _engine.nLevel = image.nLevel;

        _pages = new PageInfo[ doc.pages ];
        for ( int page = 0 ; page < _pages.length ; page++ ) {
            _pages[ page ] = preparePageTextureHolder( gl , image );
        }

        _engine.pageSize = new SizeInfo( image.width , image.height );
        _engine.pages = doc.pages;

        _loader.load( _engine.page - 1 );
        _loader.load( _engine.page );
        _loader.load( _engine.page + 1 );
    }

    private TextureInfo prepareBackgroundTexture( final GL10 gl , final Resources res ) {
        final Bitmap source = BitmapFactory.decodeResource( res , R.drawable.background );

        final int size = toTextureSize( Math.max( _engine.surfaceSize.width , _engine.surfaceSize.height ) );
        final Bitmap bitmap = Bitmap.createBitmap( size , size , Config.ARGB_8888 );

        final Canvas c = new Canvas( bitmap );
        final Paint p = new Paint();

        for ( int y = 0 ; y * source.getHeight() < _engine.surfaceSize.height ; y++ ) {
            for ( int x = 0 ; x * source.getWidth() < _engine.surfaceSize.width ; x++ ) {
                c.drawBitmap( source , x * source.getWidth() , y * source.getHeight() , p );
            }
        }

        source.recycle();

        final TextureInfo ret =
                new TextureInfo( genTexture( gl ) , _engine.surfaceSize.width , _engine.surfaceSize.height );

        bindTexture( gl , ret , bitmap );

        bitmap.recycle();

        return ret;
    }

    private PageInfo preparePageTextureHolder( final GL10 gl , final ImageInfo image ) {
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

    private TextureInfo prepareTexture( final GL10 gl , final Resources res , final int resId ) {
        final Bitmap bitmap = BitmapFactory.decodeResource( res , resId );

        final TextureInfo ret = new TextureInfo( genTexture( gl ) , bitmap.getWidth() , bitmap.getHeight() );

        bindTexture( gl , ret , bitmap );

        bitmap.recycle();

        return ret;
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

    private int toTextureSize( final int value ) {
        for ( int size = 1 ; ; size <<= 1 ) {
            if ( size >= value ) {
                return size;
            }
        }
    }

    private void unbindPageImage( final GL10 gl , final LoadBitmapTask task ) {
        _pages[ task.page ].statuses[ task.level ][ task.py ][ task.px ] = PageTextureStatus.UNBIND;

        gl.glDeleteTextures( 1 ,
                new int[] { _pages[ task.page ].textures[ task.level ][ task.py ][ task.px ].texture } , 0 );
    }

    void zoom( final float scaleDelta , final PointF center ) {
        _engine.zoom( scaleDelta , center );
    }

    void zoomByLevel( final int delta ) {
        _engine.zoomByLevel( delta );
    }
}
