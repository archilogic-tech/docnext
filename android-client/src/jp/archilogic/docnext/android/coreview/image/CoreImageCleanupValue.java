package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.info.SizeFInfo;
import jp.archilogic.docnext.android.info.SizeInfo;
import android.graphics.PointF;
import android.os.SystemClock;

public class CoreImageCleanupValue {
    static CoreImageCleanupValue getDoubleTapInstance( final CoreImageMatrix matrix , final SizeInfo surface ,
            final float minScale , final float maxScale , final PointF point , final SizeFInfo padding ) {
        float scale;

        if ( matrix.scale < maxScale ) {
            // 1.01 for rounding
            final float delta =
                    ( float ) Math.pow( maxScale / minScale , 1.01 / getNumberOfZoomLevel( minScale , maxScale ) );

            scale = Math.min( maxScale , delta * matrix.scale );
        } else {
            scale = minScale;
        }

        return getZoomInstance( matrix , surface , point , padding , scale );
    }

    static CoreImageCleanupValue getFlingInstance( final CoreImageMatrix matrix , final PointF velocity ) {
        final CoreImageCleanupValue ret = new CoreImageCleanupValue();

        ret.copy( matrix );

        ret.dstScale = matrix.scale;

        ret.dstX = matrix.tx + velocity.x / 4;
        ret.dstY = matrix.ty + velocity.y / 4;

        ret.start = SystemClock.elapsedRealtime();
        ret.shouldAdjust = true;

        return ret;
    }

    static CoreImageCleanupValue getInstance( final CoreImageMatrix matrix , final SizeInfo surface ,
            final SizeInfo page , final float minScale , final float maxScale ) {
        if ( matrix.tx < Math.min( surface.width - page.width * matrix.scale , 0 ) || //
                matrix.ty < Math.min( surface.height - page.height * matrix.scale , 0 ) || //
                matrix.tx > 0 || matrix.ty > 0 || matrix.scale < minScale || matrix.scale > maxScale ) {
            final CoreImageCleanupValue ret = new CoreImageCleanupValue();

            ret.copy( matrix );

            if ( matrix.scale < minScale ) {
                ret.dstScale = minScale;
                ret.dstX = 0;
                ret.dstY = 0;
            } else if ( matrix.scale > maxScale ) {
                ret.dstScale = maxScale;
                ret.dstX = ( maxScale * matrix.tx - ( maxScale - matrix.scale ) * surface.width / 2 ) / matrix.scale;
                ret.dstY = ( maxScale * matrix.ty - ( maxScale - matrix.scale ) * surface.height / 2 ) / matrix.scale;
            } else {
                ret.dstScale = matrix.scale;
                ret.dstX = matrix.tx;
                ret.dstY = matrix.ty;
            }

            ret.start = SystemClock.elapsedRealtime();
            ret.shouldAdjust = false;

            return ret.adjust( surface , page );
        } else {
            return null;
        }
    }

    static CoreImageCleanupValue getLevelZoomInstance( final CoreImageMatrix matrix , final SizeInfo surface ,
            final float minScale , final float maxScale , final PointF point , final SizeFInfo padding , final int delta ) {
        // 1.01 for rounding
        final float scaleDelta =
                ( float ) Math.pow( maxScale / minScale , 1.01 / getNumberOfZoomLevel( minScale , maxScale ) * delta );

        final float scale = Math.max( minScale , Math.min( maxScale , scaleDelta * matrix.scale ) );

        return getZoomInstance( matrix , surface , point , padding , scale );
    }

    private static int getNumberOfZoomLevel( final float minScale , final float maxScale ) {
        return ( int ) Math.floor( Math.log( maxScale / minScale ) / Math.log( 2 ) );
    }

    private static CoreImageCleanupValue getZoomInstance( final CoreImageMatrix matrix , final SizeInfo surface ,
            final PointF point , final SizeFInfo padding , final float scale ) {
        final CoreImageCleanupValue ret = new CoreImageCleanupValue();

        ret.copy( matrix );

        ret.dstScale = scale;

        ret.dstX = ret.dstScale / matrix.scale * ( matrix.tx - ( point.x - padding.width ) ) + surface.width / 2;
        ret.dstY = ret.dstScale / matrix.scale * ( matrix.ty - ( point.y - padding.height ) ) + surface.height / 2;

        ret.start = SystemClock.elapsedRealtime();
        ret.shouldAdjust = true;

        return ret;
    }

    float srcScale;
    float srcX;
    float srcY;
    float dstScale;
    float dstX;
    float dstY;

    boolean shouldAdjust;
    long start;

    private CoreImageCleanupValue adjust( final SizeInfo surface , final SizeInfo page ) {
        dstX = Math.min( Math.max( dstX , surface.width - page.width * dstScale ) , 0 );
        dstY = Math.min( Math.max( dstY , surface.height - page.height * dstScale ) , 0 );

        return this;
    }

    private void copy( final CoreImageMatrix matrix ) {
        srcScale = matrix.scale;
        srcX = matrix.tx;
        srcY = matrix.ty;
    }
}
