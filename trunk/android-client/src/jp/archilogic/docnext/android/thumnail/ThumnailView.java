package jp.archilogic.docnext.android.thumnail;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;

public class ThumnailView extends FrameLayout implements CoreView {
    private long _id = -1;

    private CoreViewDelegate _delegate;

    private Context _context;
    
    private GridView _gridView;

    public ThumnailView(final Context context) {
        super( context );

        _context = context;
        LayoutInflater.from( context ).inflate( R.layout.thumnail , this , true );
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
    }

    @Override
    public void onDragGesture( final PointF delta ) {
    }

    @Override
    public void onFlingGesture( final PointF velocity ) {
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
    public void onTapGesture( final PointF point ) {
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
    }

    @Override
    public void setDelegate( final CoreViewDelegate argumentDelegate ) {
        _delegate = argumentDelegate;
    }

    private void setGridView() {
        _gridView = (GridView) findViewById( R.id.thumnail );
        _gridView.setAdapter( new ThumbnailImageAdapter( _context , _id ) );

        _gridView.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick( final AdapterView< ? > parent , final View v ,
                    final int position , final long _id ) {
                final Intent intent = new Intent();
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , position );
                _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
            }
        } );
    }

    @Override
    public void setIds( final long[] ids ) {
        _id = ids[ 0 ];
        setGridView();
    }

}
