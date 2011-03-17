package jp.archilogic.docnext.android.coreview.image;

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

    CoreImageMatrix matrix = new CoreImageMatrix();
    SizeInfo pageSize;
    SizeInfo surfaceSize;
    boolean isInteracting = false;

    private float _minScale;
    private float _maxScale;

    private CoreImageCleanupValue cleanup = null;
    private final Interpolator interpolator = new DecelerateInterpolator();

    void drag( final PointF delta ) {
        matrix.tx += delta.x;
        matrix.ty += delta.y;
    }

    void initScale() {
        matrix.scale = Math.min( 1f * surfaceSize.width / pageSize.width , 1f * surfaceSize.height / pageSize.height );

        _minScale = matrix.scale;
        _maxScale = 1f;
    }

    /**
     * Check cleanup, Check change page, etc...
     */
    void update() {
        if ( !isInteracting ) {
            if ( cleanup == null ) {
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

        matrix.tx = matrix.tx * scaleDelta + ( 1 - scaleDelta ) * center.x;
        matrix.ty = matrix.ty * scaleDelta + ( 1 - scaleDelta ) * center.y;
    }
}
