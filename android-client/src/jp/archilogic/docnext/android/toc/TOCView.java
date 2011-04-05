package jp.archilogic.docnext.android.toc;

import java.util.ArrayList;
import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.TOCElement;
import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

public class TOCView extends FrameLayout implements CoreView {
    private final ArrayAdapter< String > tocArrayAdapter;

    private ArrayList< Integer > pageList;
    private final OnItemClickListener mTcoClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick( final AdapterView< ? > av , final View v , final int arg2 , final long arg3 ) {
            final Intent intent = new Intent();
            intent.putExtra( CoreViewActivity.EXTRA_PAGE , pageList.get( arg2 ) );
            delegate.changeCoreViewType( DocumentType.IMAGE , intent );
        }
    };

    private CoreViewDelegate delegate;

    public TOCView( final Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.toc , this , true );
        tocArrayAdapter = new ArrayAdapter< String >( getContext() , R.layout.toc_title );
        final ListView tocListView = ( ListView ) findViewById( R.id.table_of_contents_listview );
        tocListView.setAdapter( tocArrayAdapter );
        tocListView.setOnItemClickListener( mTcoClickListener );

        findViewById( R.id.table_of_contents_listview ).setVisibility( View.VISIBLE );

    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragGesture( final PointF delta ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFlingGesture( final PointF velocity ) {
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
    public void onPause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTapGesture( final PointF point ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDelegate( final CoreViewDelegate argumentDelegate ) {
        delegate = argumentDelegate;
    }

    @Override
    public void setIds( final long[] ids ) {
        pageList = new ArrayList< Integer >();
        final List< TOCElement > toc = Kernel.getLocalProvider().getTableOfContentsInfo( ids[ 0 ] );

        if ( toc == null ) {
            return;
        }

        for ( final TOCElement element : toc ) {
            pageList.add( element.page );
            tocArrayAdapter.add( element.text );
        }
    }
}
