package jp.archilogic.docnext.android.bookmark;

import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.BookmarkInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class BookmarkView extends LinearLayout implements CoreView {

    private long _id;
    private final String TAG = BookmarkView.class.getName();
    private CoreViewDelegate _delegate;
    
    private OnItemClickListener _ItemclickListener = new OnItemClickListener() {
        @Override
        public void onItemClick( AdapterView< ? > arg0 , View arg1 , int arg2 , long arg3 ) {
            Log.d( TAG , "Hello " + arg2 + " arg3: " + arg3 );
            List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
            final Intent intent = new Intent();
            intent.putExtra( CoreViewActivity.EXTRA_PAGE , bookmarks.get( arg2 ).page );
            _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
        }
    };

    public BookmarkView(Context context) {
        super( context );
        
        Log.d( TAG , "BookmarkView" );
    }
    
    private void initListView() {
        List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
        if ( bookmarks == null ) {
            return;
        }

        ListView listView = new ListView( getContext() );
        ArrayAdapter< String > adapter = new ArrayAdapter< String >( getContext(), R.layout.toc_title );
        for ( BookmarkInfo bookmark : bookmarks ) {
            adapter.add( bookmark.text );
        }
        listView.setAdapter( adapter );
        listView.setOnItemClickListener( _ItemclickListener );
        addView( listView );
    }

    @Override
    public void onDoubleTapGesture( PointF point ) {
    }

    @Override
    public void onDragGesture( PointF delta ) {
        
    }

    @Override
    public void onFlingGesture( PointF velocity ) {
        
    }

    @Override
    public void onGestureBegin() {
        
    }

    @Override
    public void onGestureEnd() {
        
    }

    @Override
    public void onMenuVisibilityChange( boolean isMenuVisible ) {
        
    }

    @Override
    public void onPause() {
        
    }

    @Override
    public void onResume() {
        
    }

    @Override
    public void onTapGesture( PointF point ) {
        
    }

    @Override
    public void onZoomGesture( float scaleDelta , PointF center ) {
        
    }
    
    @Override
    public void restoreState( Bundle state ) {
    }

    @Override
    public void setDelegate( CoreViewDelegate delegate ) {
        _delegate = delegate;
    }

    @Override
    public void setIds( long[] ids ) {
        _id = ids[ 0 ];
        initListView();
    }

    @Override
    public void saveState( Bundle state ) {
    }
}
