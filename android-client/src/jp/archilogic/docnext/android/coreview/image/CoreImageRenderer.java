package jp.archilogic.docnext.android.coreview.image;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.image.CoreImageState.OnPageChangeListener;
import jp.archilogic.docnext.android.coreview.image.CoreImageState.OnPageChangedListener;
import jp.archilogic.docnext.android.coreview.image.CoreImageState.OnScaleChangeListener;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.info.SizeInfo;
import jp.archilogic.docnext.android.service.DownloadService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
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

    private Context _context;
    private CoreImageState _state = new CoreImageState();
    private CoreImageRenderEngine _renderEngine = new CoreImageRenderEngine();

    private final Queue< LoadBitmapTask > _bindQueue = Lists.newLinkedList();
    private final Queue< LoadBitmapTask > _unbindQueue = Lists.newLinkedList();
    private final ImageLoadQueue _imageLoadQueue = new ImageLoadQueue();
    private ExecutorService _executor = new ThreadPoolExecutor( 1 , 1 , 0L , TimeUnit.MILLISECONDS ,
            _imageLoadQueue );
    private final Map< Integer , List< LoadBitmapTask > > _tasks = Maps.newHashMap();

    int _fpsCounter = 0;
    long _fpsTime;
    long _frameSum;

    private final PageLoader _loader = new PageLoader() {
        @Override
        public void load( final int page ) {
            if ( page < 0 || page >= _state.pages ) {
                return;
            }

            if ( _tasks.get( page ) == null ) {
                _tasks.put( page , Lists.< LoadBitmapTask > newArrayList() );
            }

            final int[][] dimen = _renderEngine.getTextureDimension( page );
            for ( int level = 0 ; level < _state.nLevel ; level++ ) {
                for ( int py = 0 ; py < dimen[ level ][ 1 ] ; py++ ) {
                    for ( int px = 0 ; px < dimen[ level ][ 0 ] ; px++ ) {
                        if ( !Kernel.getLocalProvider().isImageExists( _state.id , page , level , px , py ) ) {
                            continue;
                        }
                        
                        final LoadBitmapTask task =
                                new LoadBitmapTask( _state , page , level , px , py , _tasks ,
                                        _bindQueue );

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
                    if ( task != null ) {
                        task.cancel();
                    }
                }

                _tasks.remove( page );
            }

            synchronized ( _unbindQueue ) {
                // unbind all
                final int[][] dimen = _renderEngine.getTextureDimension( page );
                for ( int level = 0 ; level < _state.nLevel ; level++ ) {
                    for ( int py = 0 ; py < dimen[ level ][ 1 ] ; py++ ) {
                        for ( int px = 0 ; px < dimen[ level ][ 0 ] ; px++ ) {
                            _unbindQueue.add( new LoadBitmapTask( null , page , level , px , py ,
                                    null , null ) );
                        }
                    }
                }
            }
        }
    };

    private final OnPageChangeListener _pageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageChange( final int page ) {
            _imageLoadQueue.setPage( page );
        }
    };

    private final OnPageChangedListener _pageChangedListener = new OnPageChangedListener() {
        @Override
        public void onPageChanged( final int page ) {
            _context.sendBroadcast( new Intent( HasPage.BROADCAST_PAGE_CHANGED ) );
        }
    };

    private final BroadcastReceiver _remoteProviderReceiver = new BroadcastReceiver() {
        private void loadBitmap( final Intent intent ) {
            int page = intent.getIntExtra( DownloadService.EXTRA_PAGE , -1 );
            int level = intent.getIntExtra( DownloadService.EXTRA_LEVEL , -1 );
            int px = intent.getIntExtra( DownloadService.EXTRA_PX , -1 );
            int py = intent.getIntExtra( DownloadService.EXTRA_PY , -1 );

            if ( _state != null &&
                    Kernel.getLocalProvider().isImageExists( _state.id , page , level , px , py ) ) {

                if ( _tasks.get( page ) == null ) {
                    _tasks.put( page , Lists.< LoadBitmapTask > newArrayList() );
                }

                final LoadBitmapTask task =
                    new LoadBitmapTask( _state , page , level , px , py , _tasks ,
                            _bindQueue );

                _tasks.get( page ).add( task );
                _executor.execute( task );
            } else {
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        loadBitmap( intent );
                    }
                }, 1000 );
            }
        }
        
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            
            if ( intent.getAction().equals( DownloadService.BROADCAST_DOWNLOAD_DOWNLOADED) ) {
                loadBitmap( intent );
            }
        }
    };

    public CoreImageRenderer( final Context context ) {
        _context = context;
        _state.setPageLoader( _loader );
        _state.setOnPageChangeListener( _pageChangeListener );
        _state.setOnPageChangedListener( _pageChangedListener );

        final IntentFilter filter = new IntentFilter();
        filter.addAction( DownloadService.BROADCAST_DOWNLOAD_DOWNLOADED );
        context.registerReceiver( _remoteProviderReceiver , filter );
    }

    void beginInteraction() {
        _state.isInteracting = true;
    }

    void cleanup() {
        _renderEngine.cleanup();
        _executor.shutdownNow();

        _context = null;
        _state = null;
        _renderEngine = null;
        _bindQueue.clear();
        _unbindQueue.clear();
        _imageLoadQueue.clear();
        _executor = null;
        _tasks.clear();
    }

    void doubleTap( final PointF point ) {
        _state.doubleTap( point );
    }

    void drag( final PointF delta ) {
        _state.drag( delta );
    }

    void endInteraction() {
        _state.isInteracting = false;
    }

    void fling( final PointF velocity ) {
        _state.fling( velocity );
    }

    CoreImageDirection getDirection() {
        return _state.direction;
    }

    int getPage() {
        return _state.page;
    }

    @Override
    public void onDrawFrame( final GL10 gl ) {
        final long t = SystemClock.elapsedRealtime();

        while ( !_bindQueue.isEmpty() ) {
            _renderEngine.bindPageImage( _bindQueue.poll() );
        }

        synchronized ( _unbindQueue ) {
            while ( !_unbindQueue.isEmpty() ) {
                _renderEngine.unbindPageImage( _unbindQueue.poll() );
            }
        }

        if ( _state != null ) {
            _state.update();
            if ( _renderEngine != null ) {
                _renderEngine.render( _state );
            }
        }

        _fpsCounter++;
        _frameSum += SystemClock.elapsedRealtime() - t;
        if ( _fpsCounter == 120 ) {
            // System.err.println( "FPS: " + 120.0 * 1000
            // / ( SystemClock.elapsedRealtime() - _fpsTime ) + ", avg: " + _frameSum / 120.0 );

            _fpsTime = SystemClock.elapsedRealtime();
            _fpsCounter = 0;
            _frameSum = 0;

            System.err.println( "Mem: " + Runtime.getRuntime().freeMemory() + ", "
                    + Runtime.getRuntime().maxMemory() );
        }
    }

    @Override
    public void onSurfaceChanged( final GL10 gl , final int width , final int height ) {
        GLES10.glEnable( GLES10.GL_TEXTURE_2D );
        GLES10.glEnable( GLES10.GL_BLEND );
        GLES10.glBlendFunc( GLES10.GL_SRC_ALPHA , GLES10.GL_ONE_MINUS_SRC_ALPHA );

        _state.surfaceSize = new SizeInfo( width , height );
        _state.initScale();

        _renderEngine.prepare( _context , _state.pages , _state.nLevel , _state.pageSize ,
                _state.surfaceSize );

        _loader.load( _state.page - 1 );
        _loader.load( _state.page );
        _loader.load( _state.page + 1 );

        final int[] caps =
                { GLES11.GL_ALPHA_TEST , GLES11.GL_BLEND , GLES11.GL_CLIP_PLANE0 ,
                        GLES11.GL_CLIP_PLANE1 , GLES11.GL_CLIP_PLANE2 , GLES11.GL_CLIP_PLANE3 ,
                        GLES11.GL_CLIP_PLANE4 , GLES11.GL_CLIP_PLANE5 , GLES11.GL_COLOR_LOGIC_OP ,
                        GLES11.GL_COLOR_MATERIAL , GLES11.GL_CULL_FACE , GLES11.GL_DEPTH_TEST ,
                        GLES11.GL_DITHER , GLES11.GL_FOG , GLES11.GL_LIGHT0 , GLES11.GL_LIGHT1 ,
                        GLES11.GL_LIGHT2 , GLES11.GL_LIGHT3 , GLES11.GL_LIGHT4 , GLES11.GL_LIGHT5 ,
                        GLES11.GL_LIGHT6 , GLES11.GL_LIGHT7 , GLES11.GL_LIGHTING ,
                        GLES11.GL_LINE_SMOOTH , GLES11.GL_MULTISAMPLE , GLES11.GL_NORMALIZE ,
                        GLES11.GL_POINT_SMOOTH , GLES11.GL_POLYGON_OFFSET_FILL ,
                        GLES11.GL_RESCALE_NORMAL , GLES11.GL_SAMPLE_ALPHA_TO_COVERAGE ,
                        GLES11.GL_SAMPLE_ALPHA_TO_ONE , GLES11.GL_SAMPLE_COVERAGE ,
                        GLES11.GL_SCISSOR_TEST , GLES11.GL_STENCIL_TEST , GLES11.GL_TEXTURE_2D };
        final String[] names =
                { "GL11.GL_ALPHA_TEST" , "GL11.GL_BLEND" , "GL11.GL_CLIP_PLANE0" ,
                        "GL11.GL_CLIP_PLANE1" , "GL11.GL_CLIP_PLANE2" , "GL11.GL_CLIP_PLANE3" ,
                        "GL11.GL_CLIP_PLANE4" , "GL11.GL_CLIP_PLANE5" , "GL11.GL_COLOR_LOGIC_OP" ,
                        "GL11.GL_COLOR_MATERIAL" , "GL11.GL_CULL_FACE" , "GL11.GL_DEPTH_TEST" ,
                        "GL11.GL_DITHER" , "GL11.GL_FOG" , "GL11.GL_LIGHT0" , "GL11.GL_LIGHT1" ,
                        "GL11.GL_LIGHT2" , "GL11.GL_LIGHT3" , "GL11.GL_LIGHT4" , "GL11.GL_LIGHT5" ,
                        "GL11.GL_LIGHT6" , "GL11.GL_LIGHT7" , "GL11.GL_LIGHTING" ,
                        "GL11.GL_LINE_SMOOTH" , "GL11.GL_MULTISAMPLE" , "GL11.GL_NORMALIZE" ,
                        "GL11.GL_POINT_SMOOTH" , "GL11.GL_POLYGON_OFFSET_FILL" ,
                        "GL11.GL_RESCALE_NORMAL" , "GL11.GL_SAMPLE_ALPHA_TO_COVERAGE" ,
                        "GL11.GL_SAMPLE_ALPHA_TO_ONE" , "GL11.GL_SAMPLE_COVERAGE" ,
                        "GL11.GL_SCISSOR_TEST" , "GL11.GL_STENCIL_TEST" , "GL11.GL_TEXTURE_2D" };

        for ( int index = 0 ; index < caps.length ; index++ ) {
            System.err.println( names[ index ] + ": " + GLES11.glIsEnabled( caps[ index ] ) );
        }
    }

    @Override
    public void onSurfaceCreated( final GL10 gl , final EGLConfig config ) {
        final DocInfo doc = Kernel.getLocalProvider().getDocInfo( _state.id );
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( _state.id );
        _state.nLevel = image.nLevel;

        _state.pageSize = new SizeInfo( image.width , image.height );
        _state.pages = doc.pages;
    }

    void setDirection( final CoreImageDirection direction ) {
        _state.direction = direction;
    }

    void setId( final long id ) {
        _state.id = id;
    }

    void setOnScaleChangeListener( final OnScaleChangeListener l ) {
        _state.setOnScaleChangeListener( l );
    }

    void setPage( final int page ) {
        _state.page = page;
    }

    void tap( final PointF point ) {
        _state.tap( point );
    }

    void zoom( final float scaleDelta , final PointF center ) {
        _state.zoom( scaleDelta , center );
    }

    void zoomByLevel( final int delta ) {
        _state.zoomByLevel( delta );
    }
}
