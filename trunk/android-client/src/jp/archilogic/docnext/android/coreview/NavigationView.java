package jp.archilogic.docnext.android.coreview;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.LinearLayout;

public abstract class NavigationView extends LinearLayout implements CoreView , HasPage {

    protected CoreViewDelegate _delegate;
    protected long _id;
    protected int _page;

    public NavigationView( Context context ) {
        super( context );
        
        setOrientation( VERTICAL );
    }

    @Override
    public boolean dispatchKeyEvent( KeyEvent e ) {
        if ( e.getKeyCode() == KeyEvent.KEYCODE_BACK && 
                e.getAction() == KeyEvent.ACTION_UP ) {
            _delegate.back();
            return true;
        }
        return false;
    }

    @Override
    public int getPage() {
        return _page;
    }
    
    public abstract void init();

    @Override
    public void onDoubleTapGesture( PointF point ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDragGesture( PointF delta ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onFlingGesture( PointF velocity ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGestureBegin() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGestureEnd() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMenuVisibilityChange( boolean isMenuVisible ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        
    }
    
    protected void goTo( int page ) {
        if ( !Kernel.getLocalProvider().isAllImageExists( _id , page ) ) {
            return;
        }
        final Intent intent = new Intent();
        intent.putExtra( CoreViewActivity.EXTRA_PAGE , page );
        _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
    }

    @Override
    public void onTapGesture( PointF point ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onZoomGesture( float scaleDelta , PointF center ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restoreState( Bundle state ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void saveState( Bundle state ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDelegate( CoreViewDelegate delegate ) {
        _delegate = delegate;
    }
    
    @Override
    public void setIds( long[] ids ) {
        _id = ids[ 0 ];
    }
    
    @Override
    public void setPage( int page ) {
        _page = page;
    }
}
