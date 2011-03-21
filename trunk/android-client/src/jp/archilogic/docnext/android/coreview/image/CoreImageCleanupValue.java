package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.info.SizeInfo;
import android.graphics.PointF;
import android.os.SystemClock;

public class CoreImageCleanupValue {
    static CoreImageCleanupValue getDoubleTapInstance( final CoreImageMatrix matrix , final SizeInfo surface ,
            final SizeInfo page , final float minScale , final float maxScale , final PointF point ,
            final float horizontalPadding , final float verticalPadding ) {
        final CoreImageCleanupValue ret = new CoreImageCleanupValue();

        ret.copy( matrix );

        if ( matrix.scale < maxScale ) {
            ret.dstScale = maxScale;
        } else {
            ret.dstScale = minScale;
        }

        ret.dstX =
                matrix.tx * maxScale / matrix.scale - ( point.x - horizontalPadding ) * ( maxScale - matrix.scale )
                        / matrix.scale;
        ret.dstY =
                matrix.ty * maxScale / matrix.scale - ( point.y - verticalPadding ) * ( maxScale - matrix.scale )
                        / matrix.scale;

        ret.adjust( surface , page );

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

            ret.adjust( surface , page );

            return ret;
        } else {
            return null;
        }
    }

    float srcScale;
    float srcX;
    float srcY;
    float dstScale;
    float dstX;
    float dstY;

    long start;

    private void adjust( final SizeInfo surface , final SizeInfo page ) {
        dstX = Math.min( Math.max( dstX , Math.min( surface.width - page.width * dstScale , 0 ) ) , 0 );
        dstY = Math.min( Math.max( dstY , Math.min( surface.height - page.height * dstScale , 0 ) ) , 0 );

        start = SystemClock.elapsedRealtime();
    }

    private void copy( final CoreImageMatrix matrix ) {
        srcScale = matrix.scale;
        srcX = matrix.tx;
        srcY = matrix.ty;
    }
}
