package jp.archilogic.docnext.android.core.text;

import android.graphics.PointF;

class CleanUpState {
    public static CleanUpState getInstance( final PointF offset , final float scale , final PointF offsetTo ,
            final float scaleTo ) {
        final CleanUpState ret = new CleanUpState();

        ret.needCleanUp = true;

        ret.srcOffset = new PointF( offset.x , offset.y );
        ret.srcScale = scale;
        ret.dstOffset = new PointF( offsetTo.x , offsetTo.y );
        ret.dstScale = scaleTo;

        return ret;
    }

    public static CleanUpState getInstance( final PointF offset , final float scale , final Size surface ,
            final Size image , final float minScale , final float maxScale ) {
        final CleanUpState ret = new CleanUpState();

        if ( offset.x < Math.min( surface.width - image.width * scale , 0 ) || //
                offset.y < Math.min( surface.height - image.height * scale , 0 ) || //
                offset.x > 0 || offset.y > 0 || scale < minScale || scale > maxScale ) {
            ret.needCleanUp = true;

            ret.srcOffset = new PointF( offset.x , offset.y );
            ret.srcScale = scale;

            if ( scale < minScale ) {
                ret.dstScale = minScale;
                ret.dstOffset = new PointF( 0 , 0 );
            } else if ( scale > maxScale ) {
                ret.dstScale = maxScale;
                ret.dstOffset =
                        new PointF( ( maxScale * offset.x - ( maxScale - scale ) * surface.width / 2 ) / scale ,
                                ( maxScale * offset.y - ( maxScale - scale ) * surface.height / 2 ) / scale );
            } else {
                ret.dstScale = scale;
                ret.dstOffset = new PointF( offset.x , offset.y );
            }

            ret.dstOffset.x =
                    Math.min( Math.max( ret.dstOffset.x , Math.min( surface.width - image.width * ret.dstScale , 0 ) ) ,
                            0 );
            ret.dstOffset.y = Math.min( Math.max( ret.dstOffset.y , //
                    Math.min( surface.height - image.height * ret.dstScale , 0 ) ) , 0 );
        } else {
            ret.needCleanUp = false;
        }

        return ret;
    }

    boolean needCleanUp;

    PointF srcOffset;
    float srcScale;
    PointF dstOffset;
    float dstScale;
}
