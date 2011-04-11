package jp.archilogic.docnext.android.coreview;

import android.graphics.PointF;

public interface CoreView {
    void onDoubleTapGesture( PointF point );

    void onDragGesture( PointF delta );

    void onFlingGesture( PointF velocity );

    void onGestureBegin();

    void onGestureEnd();

    void onMenuVisibilityChange( boolean isMenuVisible );

    void onPause();

    void onResume();

    void onTapGesture( PointF point );

    /**
     * Invoked when pinch or spread
     */
    void onZoomGesture( float scaleDelta , PointF center );

    void setDelegate( CoreViewDelegate delegate );

    void setIds( long[] ids );
}
