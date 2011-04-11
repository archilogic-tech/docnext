package jp.archilogic.docnext.android.thumnail;

import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.thumnail.ThumbnailImageAdapter.Direction;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

public class GalleryView extends LinearLayout implements CoreView {
    private long _id;
    private CoverFlow _coverFlow;
    
    private Context _context;
    private CoreViewDelegate _delegate;
    
    private Direction _direction = Direction.LEFT;
    
    public GalleryView(Context context) {
        super( context );
        
        _context = context;
        
        //setOrientation( VERTICAL );
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
    public void onPause() {
    }

    @Override
    public void onResume() {
    }
    
    private void onSetId() {
        _coverFlow = new CoverFlow( _context );

        _coverFlow.setLayoutParams( new LinearLayout.LayoutParams (
                LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ) );
        _coverFlow.setAdapter( new ThumbnailImageAdapter( _context , _id , Direction.LEFT ) );
        _coverFlow.setSelection( _coverFlow.getAdapter().getCount() - 1 );

        _coverFlow.setOnItemClickListener( new OnItemClickListener() {

            @Override
            public void onItemClick( final AdapterView< ? > parent , final View v ,
                    final int position , final long _id ) {
                final Intent intent = new Intent();
                if ( _direction == Direction.LEFT ) {
                    intent.putExtra( CoreViewActivity.EXTRA_PAGE , _coverFlow.getAdapter().getCount() - position - 1 );
                } else {
                    intent.putExtra( CoreViewActivity.EXTRA_PAGE , position );
                }
                _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
            }
        } );
        
        addView( _coverFlow );
        
//        addControler();
    }

    @Override
    public void onTapGesture( PointF point ) {
    }

    @Override
    public void onZoomGesture( float scaleDelta , PointF center ) {
    }

    @Override
    public void setDelegate( CoreViewDelegate delegate ) {
        _delegate = delegate;
    }

    @Override
    public void setIds( long[] ids ) {
        _id = ids[ 0 ];
        onSetId();
    }

    @Override
    public void onMenuVisibilityChange( boolean isMenuVisible ) {
        // TODO Auto-generated method stub
        
    }
}
