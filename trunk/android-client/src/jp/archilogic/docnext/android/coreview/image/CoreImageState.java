package jp.archilogic.docnext.android.coreview.image;

import java.util.concurrent.locks.ReentrantLock;

import jp.archilogic.docnext.android.coreview.image.CoreImageRenderer.PageLoader;
import jp.archilogic.docnext.android.info.SizeFInfo;
import jp.archilogic.docnext.android.info.SizeInfo;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Handle non-OpenGL parameters
 */
public class CoreImageState {
    interface OnPageChangedListener {
        void onPageChanged( int page );
    }

    interface OnPageChangeListener {
        void onPageChange( int page );
    }

    interface OnScaleChangeListener {
        /**
         * This is invoked by future value (ie. not current value)
         */
        void onScaleChange( boolean isMin , boolean isMax );
    }

    long id;
    int page = 0;
    int pages;
    int nLevel;
    CoreImageMatrix matrix = new CoreImageMatrix();
    SizeInfo pageSize;
    SizeInfo surfaceSize;
    CoreImageDirection direction;
    boolean isInteracting = false;

    private PageLoader _loader;

    private float _minScale;
    private float _maxScale;

    private final Interpolator _interpolator = new DecelerateInterpolator();
    private CoreImageCleanupValue _cleanup = null;
    private boolean _preventCheckChangePage = false;
    private boolean _willGoNextPage = false;
    private boolean _willGoPrevPage = false;
    private OnScaleChangeListener _scaleChangeLisetener = null;
    private OnPageChangeListener _pageChangeListener = null;
    private OnPageChangedListener _pageChangedListener;
    private final ReentrantLock _lock = new ReentrantLock();

    private void changeToNextPage() {
        System.err.println( "*** changeToNextPage" );

        if ( _pageChangeListener != null ) {
            _pageChangeListener.onPageChange( page + 1 );
        }

        if ( page - 1 >= 0 ) {
            _loader.unload( page - 1 );
        }

        if ( page + 2 < pages ) {
            _loader.load( page + 2 );
        }

        page++;

        if ( _pageChangedListener != null ) {
            _pageChangedListener.onPageChanged( page );
        }

        direction.updateOffset( this , true );
    }

    private void changeToPrevPage() {
        if ( _pageChangeListener != null ) {
            _pageChangeListener.onPageChange( page - 1 );
        }

        if ( page + 1 < pages ) {
            _loader.unload( page + 1 );
        }

        if ( page - 2 >= 0 ) {
            _loader.load( page - 2 );
        }

        page--;

        if ( _pageChangedListener != null ) {
            _pageChangedListener.onPageChanged( page );
        }

        direction.updateOffset( this , false );
    }

    /**
     * @return isNext
     */
    private Boolean checkChangePage() {
        if ( direction.shouldChangeToNext( this ) && hasNextPage() ) {
            changeToNextPage();
            return true;
        } else if ( direction.shouldChangeToPrev( this ) && hasPrevPage() ) {
            changeToPrevPage();
            return false;
        }

        return null;
    }

    private void checkCleanup() {
        CoreImageCorner corner = null;

        if ( _preventCheckChangePage ) {
            _preventCheckChangePage = false;
        } else if ( _willGoNextPage ) {
            _willGoNextPage = false;

            System.err.println( "willGoNext" );

            changeToNextPage();
            corner = direction.getCorner( true );
        } else if ( _willGoPrevPage ) {
            _willGoPrevPage = false;

            changeToPrevPage();
            corner = direction.getCorner( false );
        } else {
            final float tx = matrix.tx;
            final float scale = matrix.scale;

            final Boolean isNext = checkChangePage();

            if ( isNext != null ) {
                System.err.println( "*** " + tx + ", " + scale );

                corner = direction.getCorner( isNext );
            }
        }

        _cleanup =
                CoreImageCleanupValue.getInstance( matrix , surfaceSize , pageSize , _minScale ,
                        _maxScale , corner );
    }

    void doubleTap( final PointF point ) {
        _cleanup =
                CoreImageCleanupValue.getDoubleTapInstance( matrix , surfaceSize , _minScale ,
                        _maxScale , point , new SizeFInfo( getHorizontalPadding() ,
                                getVerticalPadding() ) );

        onScaleChange( _cleanup.dstScale );
    }

    void drag( final PointF delta ) {
        final float EPS = 0.1f;

        if ( surfaceSize.width + EPS >= pageSize.width * matrix.scale
                && !direction.canMoveHorizontal() ) {
            delta.x = 0;
        }

        if ( surfaceSize.height + EPS >= pageSize.height * matrix.scale
                && !direction.canMoveVertical() ) {
            delta.y = 0;
        }

        matrix.tx -= delta.x;
        matrix.ty -= delta.y;
    }

    void fling( final PointF velocity ) {
        if ( matrix.isInPage( surfaceSize , pageSize )
                && Math.hypot( velocity.x , velocity.y ) > 1000 ) {
            _cleanup = CoreImageCleanupValue.getFlingInstance( matrix , velocity );
        }
    }

    float getHorizontalPadding() {
        return Math.max( surfaceSize.width - pageSize.width * Math.max( matrix.scale , _minScale ) ,
                0 ) / 2f;
    }

    float getVerticalPadding() {
        return Math.max(
                surfaceSize.height - pageSize.height * Math.max( matrix.scale , _minScale ) , 0 ) / 2f;
    }

    private boolean hasNextPage() {
        return page + 1 < pages;
    }

    private boolean hasPrevPage() {
        return page - 1 >= 0;
    }

    void initScale() {
        matrix.scale =
                Math.min( 1f * surfaceSize.width / pageSize.width , 1f * surfaceSize.height
                        / pageSize.height );

        _minScale = matrix.scale;
        _maxScale = ( float ) Math.pow( 2 , nLevel - 1 );

        onScaleChange( matrix.scale );
    }

    boolean isCleanup() {
        return _cleanup != null;
    }

    private void onScaleChange( final float scale ) {
        final float EPS = ( float ) 1e-5;

        if ( _scaleChangeLisetener != null ) {
            _scaleChangeLisetener.onScaleChange( scale <= _minScale + EPS , scale >= _maxScale
                    - EPS );
        }
    }

    private void runCleanup() {
        float elapsed = 1f * ( SystemClock.elapsedRealtime() - _cleanup.start ) / _cleanup.duration;
        boolean willFinish = false;

        if ( elapsed > 1f ) {
            elapsed = 1f;
            willFinish = true;
        }

        matrix.scale = _cleanup.srcScale + ( _cleanup.dstScale - _cleanup.srcScale ) * //
                _interpolator.getInterpolation( elapsed );
        matrix.tx =
                _cleanup.srcX + ( _cleanup.dstX - _cleanup.srcX )
                        * _interpolator.getInterpolation( elapsed );
        matrix.ty =
                _cleanup.srcY + ( _cleanup.dstY - _cleanup.srcY )
                        * _interpolator.getInterpolation( elapsed );

        if ( _cleanup.shouldAdjust ) {
            matrix.adjust( surfaceSize , pageSize );
        }

        if ( willFinish ) {
            _cleanup = null;
        }
    }

    void setOnPageChangedListener( final OnPageChangedListener l ) {
        _pageChangedListener = l;
    }

    void setOnPageChangeListener( final OnPageChangeListener l ) {
        _pageChangeListener = l;
    }

    void setOnScaleChangeListener( final OnScaleChangeListener l ) {
        _scaleChangeLisetener = l;
    }

    void setPageLoader( final PageLoader loader ) {
        _loader = loader;
    }

    void tap( final PointF point ) {
        final int THREASHOLD = 4;

        final float x = point.x - matrix.tx;
        final float y = point.y - matrix.ty;
        final int w = surfaceSize.width / THREASHOLD;
        final int h = surfaceSize.height / THREASHOLD;

        final int dx = x < w ? -1 : x > pageSize.width * matrix.scale - w ? 1 : 0;
        final int dy = y < h ? -1 : y > pageSize.height * matrix.scale - h ? 1 : 0;

        final int delta = dx * direction.toXSign() + dy * direction.toYSign();

        if ( delta > 0 && hasNextPage() ) {
            _willGoNextPage = true;
        } else if ( delta < 0 && hasPrevPage() ) {
            _willGoPrevPage = true;
        }
    }

    /**
     * Check cleanup, Check change page, etc...
     */
    void update() {
        _lock.lock();
        try {
            if ( !isInteracting ) {
                if ( _cleanup == null ) {
                    checkCleanup();
                }

                if ( _cleanup != null ) {
                    runCleanup();
                }
            } else {
                _cleanup = null;
            }
        } finally {
            _lock.unlock();
        }
    }

    void zoom( float scaleDelta , final PointF center ) {
        _lock.lock();
        try {
            if ( matrix.scale < _minScale || matrix.scale > _maxScale ) {
                scaleDelta = ( float ) Math.pow( scaleDelta , 0.2 );
            }

            matrix.scale *= scaleDelta;

            final float hPad = getHorizontalPadding();
            final float vPad = getVerticalPadding();
            matrix.tx = scaleDelta * ( matrix.tx - ( center.x - hPad ) ) + center.x - hPad;
            matrix.ty = scaleDelta * ( matrix.ty - ( center.y - vPad ) ) + center.y - vPad;

            _preventCheckChangePage = true;

            onScaleChange( matrix.scale );
        } finally {
            _lock.unlock();
        }
    }

    void zoomByLevel( final int delta ) {
        _cleanup =
                CoreImageCleanupValue.getLevelZoomInstance( matrix , surfaceSize , _minScale ,
                        _maxScale , new PointF( surfaceSize.width / 2 , surfaceSize.height / 2 ) ,
                        new SizeFInfo( getHorizontalPadding() , getVerticalPadding() ) , delta );

        onScaleChange( _cleanup.dstScale );
    }
}
