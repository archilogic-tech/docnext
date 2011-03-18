package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.coreview.image.CoreImageRenderer.PageLoader;
import jp.archilogic.docnext.android.info.SizeInfo;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Handle non-OpenGL parameters
 */
public class CoreImageEngine {
    private static final long CLEANUP_DURATION = 200L;

    long id;
    int page = 0;
    CoreImageMatrix matrix = new CoreImageMatrix();
    SizeInfo pageSize;
    SizeInfo surfaceSize;
    CoreImageDirection direction;
    boolean[] loaded;
    boolean isInteracting = false;

    private PageLoader _loader;

    private float _minScale;
    private float _maxScale;

    private final Interpolator interpolator = new DecelerateInterpolator();
    private CoreImageCleanupValue cleanup = null;
    private boolean _preventCheckChangePage = false;

    private void changeToNextPage() {
        if ( page - 1 >= 0 ) {
            loaded[ page - 1 ] = false;
        }

        if ( page + 2 < loaded.length ) {
            _loader.load( page + 2 );
        }

        page++;

        direction.updateOffset( this , true );
    }

    private void changeToPrevPage() {
        if ( page + 1 < loaded.length ) {
            loaded[ page + 1 ] = false;
        }

        if ( page - 2 >= 0 ) {
            _loader.load( page - 2 );
        }

        page--;

        direction.updateOffset( this , false );
    }

    private void checkChangePage() {
        if ( direction.shouldChangeToNext( this ) && page + 1 < loaded.length
                && ( page + 2 >= loaded.length || Kernel.getLocalProvider().isImageExists( id , page + 2 ) ) ) {
            changeToNextPage();
        } else if ( direction.shouldChangeToPrev( this ) && page - 1 >= 0
                && ( page - 2 < 0 || Kernel.getLocalProvider().isImageExists( id , page - 2 ) ) ) {
            changeToPrevPage();
        }
    }

    void drag( final PointF delta ) {
        if ( surfaceSize.width >= pageSize.width * matrix.scale && !direction.canMoveHorizontal() ) {
            delta.x = 0;
        }
        if ( surfaceSize.height >= pageSize.height * matrix.scale && !direction.canMoveVertical() ) {
            delta.y = 0;
        }

        matrix.tx += delta.x;
        matrix.ty += delta.y;
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
        _maxScale = 1f;
    }

    void setPageLoader( final PageLoader loader ) {
        _loader = loader;
    }

    /**
     * Check cleanup, Check change page, etc...
     */
    void update() {
        if ( !isInteracting ) {
            if ( cleanup == null ) {
                if ( _preventCheckChangePage ) {
                    _preventCheckChangePage = false;
                } else {
                    checkChangePage();
                }

                cleanup = CoreImageCleanupValue.getInstance( matrix , surfaceSize , pageSize , _minScale , _maxScale );
            }

            if ( cleanup != null ) {
                float elapsed = 1f * ( SystemClock.elapsedRealtime() - cleanup.start ) / CLEANUP_DURATION;
                boolean willFinish = false;

                if ( elapsed > 1f ) {
                    elapsed = 1f;
                    willFinish = true;
                }

                matrix.scale = cleanup.srcScale + ( cleanup.dstScale - cleanup.srcScale ) * //
                        interpolator.getInterpolation( elapsed );
                matrix.tx = cleanup.srcX + ( cleanup.dstX - cleanup.srcX ) * interpolator.getInterpolation( elapsed );
                matrix.ty = cleanup.srcY + ( cleanup.dstY - cleanup.srcY ) * interpolator.getInterpolation( elapsed );

                if ( willFinish ) {
                    cleanup = null;
                }
            }
        } else {
            cleanup = null;
        }
    }

    void zoom( float scaleDelta , final PointF center ) {
        if ( matrix.scale < _minScale || matrix.scale > _maxScale ) {
            scaleDelta = ( float ) Math.pow( scaleDelta , 0.2 );
        }

        matrix.scale *= scaleDelta;

        // This formula may be wrong...
        matrix.tx = matrix.tx * scaleDelta + ( 1 - scaleDelta ) * ( center.x - getHorizontalPadding() );
        matrix.ty = matrix.ty * scaleDelta + ( 1 - scaleDelta ) * ( center.y - getVerticalPadding() );

        _preventCheckChangePage = true;
    }
}
