package jp.archilogic.docnext.android.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ZoomControls;

public class CustomZoomControls extends ZoomControls {
    private TouchAndZoomImageSwitcher _switcher;

    private final OnClickListener onZoomInClickListener = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _switcher.zoomIn();

            setIsZoomInEnabled( _switcher.getZoomLevel() < 4 );
            setIsZoomOutEnabled( _switcher.getZoomLevel() > 0 );
        }
    };
    private final OnClickListener onZoomOutClickListener = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _switcher.zoomOut();

            setIsZoomInEnabled( _switcher.getZoomLevel() < 4 );
            setIsZoomOutEnabled( _switcher.getZoomLevel() > 0 );
        }
    };

    public CustomZoomControls( final Context context , final AttributeSet attrs ) {
        super( context , attrs );
    }

    public void init( final TouchAndZoomImageSwitcher switcher ) {
        _switcher = switcher;

        setOnZoomInClickListener( onZoomInClickListener );
        setOnZoomOutClickListener( onZoomOutClickListener );
        setIsZoomOutEnabled( false );
    }
}
