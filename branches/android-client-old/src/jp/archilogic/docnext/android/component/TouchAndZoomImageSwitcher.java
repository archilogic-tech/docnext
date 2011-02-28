package jp.archilogic.docnext.android.component;

import jp.archilogic.docnext.android.activity.ImageViewerActivity;
import jp.archilogic.docnext.android.util.ConstUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class TouchAndZoomImageSwitcher extends ImageSwitcher {
    private static final int CLICK_MIN_DISTANCE = 50;

    private ImageViewerActivity activity;

    private TranslateAnimation r2lIn;
    private TranslateAnimation r2lOut;
    private TranslateAnimation l2rIn;
    private TranslateAnimation l2rOut;

    private int currentBitmapWidth;
    private int currentBitmapHeight;
    private float baseScale;
    private int zoomLevel;
    private int screenWidth;

    private final ViewSwitcher.ViewFactory factory = new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
            return new ImageView( TouchAndZoomImageSwitcher.this.getContext() );
        }
    };

    private final View.OnTouchListener touchListener = new View.OnTouchListener() {
        private float downX;
        private float downY;
        private int scrollX;
        private int scrollY;
        private boolean inClickZone;

        @Override
        public boolean onTouch( final View v , final MotionEvent event ) {
            switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                scrollX = getScrollX();
                scrollY = getScrollY();
                inClickZone = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if ( inClickZone && Math.hypot( event.getX() - downX , event.getY() - downY ) > CLICK_MIN_DISTANCE ) {
                    inClickZone = false;
                }

                final int x = ( int ) ( scrollX + downX - event.getX() );
                final int y = ( int ) ( scrollY + downY - event.getY() );
                scrollTo( Math.min( Math.max( x , 0 ) , getMaxScrollX() ) ,
                        Math.min( Math.max( y , 0 ) , getMaxScrollY() ) );
                return true;
            case MotionEvent.ACTION_UP:
                if ( inClickZone ) {
                    if ( event.getX() < screenWidth / 2 ) {
                        activity.moveLeft();
                    } else {
                        activity.moveRight();
                    }
                }

                return true;
            }

            return false;
        }
    };

    public TouchAndZoomImageSwitcher( final Context context , final AttributeSet attrs ) {
        super( context , attrs );
    }

    private int getCurrentImageSwitcherHeight() {
        return ( int ) Math.round( currentBitmapHeight * Math.pow( 2 , zoomLevel ) * baseScale );
    }

    private int getCurrentImageSwitcherWidth() {
        return ( int ) Math.round( currentBitmapWidth * Math.pow( 2 , zoomLevel ) * baseScale );
    }

    public int getMaxScrollX() {
        return Math.max( getCurrentImageSwitcherWidth() - activity.getFrameWidth() , 0 );
    }

    public int getMaxScrollY() {
        return Math.max( getCurrentImageSwitcherHeight() - activity.getFrameHeight() , 0 );
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void init( final ImageViewerActivity activity ) {
        this.activity = activity;

        initAnimation();

        setFactory( factory );
        setOnTouchListener( touchListener );
        zoomLevel = 0;
        screenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();
    }

    private void initAnimation() {
        final Display display = activity.getWindowManager().getDefaultDisplay();

        r2lIn = new TranslateAnimation( display.getWidth() , 0 , 0 , 0 );
        r2lIn.setDuration( ConstUtil.ANIMATION_DURATION );
        r2lOut = new TranslateAnimation( 0 , -display.getWidth() , 0 , 0 );
        r2lOut.setDuration( ConstUtil.ANIMATION_DURATION );
        l2rIn = new TranslateAnimation( -display.getWidth() , 0 , 0 , 0 );
        l2rIn.setDuration( ConstUtil.ANIMATION_DURATION );
        l2rOut = new TranslateAnimation( 0 , display.getWidth() , 0 , 0 );
        l2rOut.setDuration( ConstUtil.ANIMATION_DURATION );
    }

    public void initScroll( final boolean leftToRight ) {
        scrollTo( leftToRight ? 0 : getMaxScrollX() , 0 );
    }

    public void setCurrentBitmap( final Bitmap bitmap ) {
        setImageDrawable( new BitmapDrawable( bitmap ) );
        currentBitmapWidth = bitmap.getWidth();
        currentBitmapHeight = bitmap.getHeight();

        baseScale =
                Math.min( activity.getFrameWidth() / ( float ) currentBitmapWidth , activity.getFrameHeight()
                        / ( float ) currentBitmapHeight );

        setCurrentImageSwitcherSize();

        final ImageView imageView = ( ImageView ) getCurrentView();
        final Matrix mat = new Matrix();
        mat.postScale( baseScale , baseScale );
        imageView.setImageMatrix( mat );
        invalidate();
    }

    private void setCurrentImageSwitcherSize() {
        final int w = Math.max( getCurrentImageSwitcherWidth() , activity.getFrameWidth() );
        final int h = Math.max( getCurrentImageSwitcherHeight() , activity.getFrameHeight() );

        final ImageView imageView = ( ImageView ) getCurrentView();
        imageView.setLayoutParams( new ImageSwitcher.LayoutParams( w , h ) );
        setLayoutParams( new FrameLayout.LayoutParams( w , h ) );
    }

    public void setLeftToRightAnimation() {
        setInAnimation( l2rIn );
        setOutAnimation( l2rOut );
    }

    public void setRightToLeftAnimatin() {
        setInAnimation( r2lIn );
        setOutAnimation( r2lOut );
    }

    public void zoomIn() {
        if ( zoomLevel < 4 ) {
            zoomLevel++;

            setCurrentImageSwitcherSize();

            final ImageView imageView = ( ImageView ) getCurrentView();
            final Matrix mat = new Matrix( imageView.getImageMatrix() );
            mat.postScale( 2 , 2 );
            imageView.setImageMatrix( mat );
            invalidate();

            final int offsetX = Math.min( activity.getFrameWidth() , getCurrentImageSwitcherWidth() / 2 ) / 2;
            final int x = getScrollX() * 2 + offsetX;
            final int offsetY = Math.min( activity.getFrameHeight() , getCurrentImageSwitcherHeight() / 2 ) / 2;
            final int y = getScrollY() * 2 + offsetY;
            scrollTo( x , y );
        }
    }

    public void zoomOut() {
        if ( zoomLevel > 0 ) {
            zoomLevel--;

            setCurrentImageSwitcherSize();

            final ImageView imageView = ( ImageView ) getCurrentView();
            final Matrix mat = new Matrix( imageView.getImageMatrix() );
            mat.postScale( 0.5f , 0.5f );
            imageView.setImageMatrix( mat );
            invalidate();

            final int offsetX = Math.min( activity.getFrameWidth() , getCurrentImageSwitcherWidth() * 2 ) / 4;
            final int x = getScrollX() / 2 - offsetX;
            final int offsetY = Math.min( activity.getFrameHeight() , getCurrentImageSwitcherHeight() * 2 ) / 4;
            final int y = getScrollY() / 2 - offsetY;
            scrollTo( Math.min( Math.max( x , 0 ) , getMaxScrollX() ) , Math.min( Math.max( y , 0 ) , getMaxScrollY() ) );
        }
    }

}
