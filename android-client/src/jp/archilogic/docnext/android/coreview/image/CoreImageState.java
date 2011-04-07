package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.Kernel;
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
    private OnScaleChangeListener _scaleChangeLisetener = null;
    private OnPageChangeListener _pageChangeListener = null;

    private void changeToNextPage() {
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

        direction.updateOffset( this , false );
    }

    private void checkChangePage() {
        if ( direction.shouldChangeToNext( this ) && page + 1 < pages
                && ( page + 2 >= pages || Kernel.getLocalProvider().isImageExists( id , page + 2 ) ) ) {
            changeToNextPage();
        } else if ( direction.shouldChangeToPrev( this ) && page - 1 >= 0
                && ( page - 2 < 0 || Kernel.getLocalProvider().isImageExists( id , page - 2 ) ) ) {
            changeToPrevPage();
        }
    }

    void doubleTap( final PointF point ) {
        _cleanup =
                CoreImageCleanupValue.getDoubleTapInstance( matrix , surfaceSize , _minScale , _maxScale , point ,
                        new SizeFInfo( getHorizontalPadding() , getVerticalPadding() ) );

        onScaleChange( _cleanup.dstScale );
    }

    void drag( final PointF delta ) {
        final float EPS = 0.1f;

        if ( surfaceSize.width + EPS >= pageSize.width * matrix.scale && !direction.canMoveHorizontal() ) {
            delta.x = 0;
        }

        if ( surfaceSize.height + EPS >= pageSize.height * matrix.scale && !direction.canMoveVertical() ) {
            delta.y = 0;
        }

        matrix.tx -= delta.x;
        matrix.ty -= delta.y;
    }

    void fling( final PointF velocity ) {
        if ( !shouldChangePage() && Math.hypot( velocity.x , velocity.y ) > 1000 ) {
            _cleanup = CoreImageCleanupValue.getFlingInstance( matrix , velocity );
        }
    }

    float getHorizontalPadding() {
        return Math.max( surfaceSize.width - pageSize.width * Math.max( matrix.scale , _minScale ) , 0 ) / 2f;
    }

    float getVerticalPadding() {
        return Math.max( surfaceSize.height - pageSize.height * Math.max( matrix.scale , _minScale ) , 0 ) / 2f;
    }

    void initScale() {
        matrix.scale = Math.min( 1f * surfaceSize.width / pageSize.width , 1f * surfaceSize.height / pageSize.height );

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
            _scaleChangeLisetener.onScaleChange( scale <= _minScale + EPS , scale >= _maxScale - EPS );
        }
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

    private boolean shouldChangePage() {
        return direction.shouldChangeToNext( this )
                && ( page + 2 >= pages || Kernel.getLocalProvider().isImageExists( id , page + 2 ) )
                || direction.shouldChangeToPrev( this )
                && ( page - 2 < 0 || Kernel.getLocalProvider().isImageExists( id , page - 2 ) );
    }

    /**
     * Check cleanup, Check change page, etc...
     */
    void update() {
        if ( !isInteracting ) {
            if ( _cleanup == null ) {
                if ( _preventCheckChangePage ) {
                    _preventCheckChangePage = false;
                } else {
                    checkChangePage();
                }

                _cleanup = CoreImageCleanupValue.getInstance( matrix , surfaceSize , pageSize , _minScale , _maxScale );
            }

            if ( _cleanup != null ) {
                float elapsed = 1f * ( SystemClock.elapsedRealtime() - _cleanup.start ) / _cleanup.duration;
                boolean willFinish = false;

                if ( elapsed > 1f ) {
                    elapsed = 1f;
                    willFinish = true;
                }

                matrix.scale = _cleanup.srcScale + ( _cleanup.dstScale - _cleanup.srcScale ) * //
                        _interpolator.getInterpolation( elapsed );
                matrix.tx =
                        _cleanup.srcX + ( _cleanup.dstX - _cleanup.srcX ) * _interpolator.getInterpolation( elapsed );
                matrix.ty =
                        _cleanup.srcY + ( _cleanup.dstY - _cleanup.srcY ) * _interpolator.getInterpolation( elapsed );

                if ( _cleanup.shouldAdjust ) {
                    matrix.adjust( surfaceSize , pageSize );
                }

                if ( willFinish ) {
                    _cleanup = null;
                }
            }
        } else {
            _cleanup = null;
        }
    }

    void zoom( float scaleDelta , final PointF center ) {
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
    }

    void zoomByLevel( final int delta ) {
        _cleanup =
                CoreImageCleanupValue.getLevelZoomInstance( matrix , surfaceSize , _minScale , _maxScale , new PointF(
                        surfaceSize.width / 2 , surfaceSize.height / 2 ) , new SizeFInfo( getHorizontalPadding() ,
                        getVerticalPadding() ) , delta );

        onScaleChange( _cleanup.dstScale );
    }
}
