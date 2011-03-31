package jp.archilogic.docnext.android.coreview.text;

import jp.archilogic.docnext.android.coreview.text.engine.CoreTextEngine;
import jp.archilogic.docnext.android.coreview.text.engine.HorizontalCoreTextEngine;
import jp.archilogic.docnext.android.coreview.text.engine.VerticalCoreTextEngine;
import jp.archilogic.docnext.android.info.SizeInfo;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

public enum TextDocDirection {
    HORIZONTAL , VERTICAL;

    CoreTextEngine getEngine() {
        switch ( this ) {
        case HORIZONTAL:
            return new HorizontalCoreTextEngine();
        case VERTICAL:
            return new VerticalCoreTextEngine();
        default:
            throw new RuntimeException();
        }
    }

    boolean shouldChangeToNext( final float offset , final SizeInfo cache , final SizeInfo surface ) {
        switch ( this ) {
        case HORIZONTAL:
            return offset < -( cache != null ? cache.height : surface.height );
        case VERTICAL:
            return offset > 0;
        default:
            throw new RuntimeException();
        }
    }

    boolean shouldChangeToPrev( final float offset , final SizeInfo cache , final SizeInfo surface ) {
        switch ( this ) {
        case HORIZONTAL:
            return offset > 0;
        case VERTICAL:
            return offset < -( cache != null ? cache.width : surface.width );
        default:
            throw new RuntimeException();
        }
    }

    SizeInfo toCacheSize( final TextLayoutInfo[] layouts , final SizeInfo surface , final CoreTextConfig config ) {
        switch ( this ) {
        case HORIZONTAL:
            final TextLayoutInfo last = layouts[ layouts.length - 1 ];
            return new SizeInfo( surface.width , ( int ) Math.ceil( last.y + last.height + config.verticalPadding ) );
        case VERTICAL:
            final TextLayoutInfo first = layouts[ 0 ];
            return new SizeInfo( ( int ) Math.ceil( first.x + first.width + config.horizontalPadding ) , surface.height );
        default:
            throw new RuntimeException();
        }
    }

    RectF toDrawRect( final Bitmap[] caches , final SizeInfo surface , final int index , final int delta ,
            final float pageSpace ) {
        switch ( this ) {
        case HORIZONTAL:
            return toDrawRectOnHorizontal( caches , surface , index , delta , pageSpace );
        case VERTICAL:
            return toDrawRectOnVertical( caches , surface , index , delta , pageSpace );
        default:
            throw new RuntimeException();
        }
    }

    private RectF toDrawRectOnHorizontal( final Bitmap[] caches , final SizeInfo surface , final int index ,
            final int delta , final float pageSpace ) {
        final Bitmap c = caches[ index + delta ];

        float offset = 0;

        switch ( delta ) {
        case -1:
            offset = -( ( c != null ? c.getHeight() : surface.height ) + pageSpace );
            break;
        case 0:
            offset = 0;
            break;
        case 1:
            offset = ( caches[ index ] != null ? caches[ index ].getHeight() : surface.height ) + pageSpace;
            break;
        }

        return new RectF( 0 , offset , c != null ? c.getWidth() : surface.width , offset
                + ( c != null ? c.getHeight() : surface.height ) );
    }

    private RectF toDrawRectOnVertical( final Bitmap[] caches , final SizeInfo surface , final int index ,
            final int delta , final float pageSpace ) {
        final Bitmap c = caches[ index + delta ];

        float offset = 0;

        switch ( delta ) {
        case -1:
            offset = ( caches[ index ] != null ? caches[ index ].getWidth() : surface.width ) + pageSpace;
            break;
        case 0:
            offset = 0;
            break;
        case 1:
            offset = -( ( c != null ? c.getWidth() : surface.width ) + pageSpace );
            break;
        }

        return new RectF( offset , 0 , offset + ( c != null ? c.getWidth() : surface.width ) , c != null
                ? c.getHeight() : surface.height );
    }

    int toXFactor() {
        switch ( this ) {
        case HORIZONTAL:
            return 0;
        case VERTICAL:
            return 1;
        default:
            throw new RuntimeException();
        }
    }

    int toYFactor() {
        switch ( this ) {
        case HORIZONTAL:
            return 1;
        case VERTICAL:
            return 0;
        default:
            throw new RuntimeException();
        }
    }

    float translate( final float offset , final PointF delta ) {
        switch ( this ) {
        case HORIZONTAL:
            return offset - delta.y;
        case VERTICAL:
            return offset - delta.x;
        default:
            throw new RuntimeException();
        }
    }

    float updateOffset( final float offset , final SizeInfo cache , final SizeInfo surface ,
            final CoreTextConfig config , final boolean isNext ) {
        switch ( this ) {
        case HORIZONTAL:
            return offset + ( isNext ? 1 : -1 )
                    * ( ( cache != null ? cache.height : surface.height ) + config.pageSpace );
        case VERTICAL:
            return offset + ( isNext ? -1 : 1 ) * ( ( cache != null ? cache.width : surface.width ) + config.pageSpace );
        default:
            throw new RuntimeException();
        }
    }
}
