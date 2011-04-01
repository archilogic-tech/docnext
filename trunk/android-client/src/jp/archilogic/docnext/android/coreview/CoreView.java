package jp.archilogic.docnext.android.coreview;

import android.graphics.PointF;

public interface CoreView {
    int getCurrentPage();

    void onDoubleTapGesture( PointF point );

    /**
     * Take account of flick?
     */
    void onDragGesture( PointF delta );

    void onGestureBegin();

    void onGestureEnd();

    void onPause();

    void onResume();

    void onTapGesture( PointF point );

    /**
     * Called when pinch or spread
     */
    void onZoomGesture( float scaleDelta , PointF center );

    void setDelegate( CoreViewDelegate delegate );

    void setIds( long[] ids );
}
