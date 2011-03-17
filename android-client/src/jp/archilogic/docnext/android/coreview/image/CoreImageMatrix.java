package jp.archilogic.docnext.android.coreview.image;

public class CoreImageMatrix {
    float scale;
    float tx;
    float ty;

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
