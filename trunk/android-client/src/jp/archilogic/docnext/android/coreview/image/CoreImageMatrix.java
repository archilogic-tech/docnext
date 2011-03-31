package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.info.SizeInfo;

public class CoreImageMatrix {
    float scale;
    float tx;
    float ty;

    CoreImageMatrix() {
    }

    CoreImageMatrix( final CoreImageMatrix o ) {
        scale = o.scale;
        tx = o.tx;
        ty = o.ty;
    }

    void adjust( final SizeInfo surface , final SizeInfo page ) {
        tx = Math.min( Math.max( tx , Math.min( surface.width - page.width * scale , 0 ) ) , 0 );
        ty = Math.min( Math.max( ty , Math.min( surface.height - page.height * scale , 0 ) ) , 0 );
    }

    float length( final float length ) {
        return length * scale;
    }

    float x( final float x ) {
        return x * scale + tx;
    }

    float y( final float y ) {
        return y * scale + ty;
    }
}
