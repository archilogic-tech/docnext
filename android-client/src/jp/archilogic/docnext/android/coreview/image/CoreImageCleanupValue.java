package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.info.SizeInfo;
import android.os.SystemClock;

public class CoreImageCleanupValue {
    static CoreImageCleanupValue getInstance( final CoreImageMatrix matrix , final SizeInfo surface ,
            final SizeInfo page , final float minScale , final float maxScale ) {
        if ( matrix.tx < Math.min( surface.width - page.width * matrix.scale , 0 ) || //
                matrix.ty < Math.min( surface.height - page.height * matrix.scale , 0 ) || //
                matrix.tx > 0 || matrix.ty > 0 || matrix.scale < minScale || matrix.scale > maxScale ) {
            final CoreImageCleanupValue ret = new CoreImageCleanupValue();

            ret.srcScale = matrix.scale;
            ret.srcX = matrix.tx;
            ret.srcY = matrix.ty;

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

            ret.dstX = Math.min( Math.max( ret.dstX , Math.min( surface.width - page.width * ret.dstScale , 0 ) ) , 0 );
            ret.dstY =
                    Math.min( Math.max( ret.dstY , Math.min( surface.height - page.height * ret.dstScale , 0 ) ) , 0 );

            ret.start = SystemClock.elapsedRealtime();

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
}
