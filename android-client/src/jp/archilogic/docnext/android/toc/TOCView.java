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
import android.widget.Toast;

public class TOCView extends FrameLayout implements CoreView {
    private ArrayAdapter<String> tocArrayAdapter;
    private ArrayList<Integer> pageList;

    private OnItemClickListener mTcoClickListener = new OnItemClickListener() {
        public void onItemClick( AdapterView<?> av, View v, int arg2, long arg3 ) {
            Intent intent = new Intent();
            intent.putExtra( CoreViewActivity.EXTRA_PAGE, pageList.get( arg2 ) );
            delegate.changeCoreViewType( DocumentType.IMAGE, intent );
        }
    };
    private CoreViewDelegate delegate;

    public TOCView(Context context) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.toc , this , true );
        tocArrayAdapter = new ArrayAdapter<String>( getContext(), R.layout.toc_title );
        ListView tocListView = ( ListView )findViewById( R.id.table_of_contents_listview );
        tocListView.setAdapter( tocArrayAdapter );
        tocListView.setOnItemClickListener( mTcoClickListener );

        findViewById( R.id.table_of_contents_listview ).setVisibility( View.VISIBLE  );

    }
    
    public void onFlingGesture( PointF velocity ) {}
    public void onDoubleTapGesture( PointF point ) {}
    public void onDragGesture( PointF delta ) {}
    public void onGestureBegin() {}
    public void onGestureEnd() {}
    public void onPause() {}
    public void onResume() {}
    public void onTapGesture( PointF point ) {}
    public void onZoomGesture( float scaleDelta, PointF center ) {}
    public void setDelegate( CoreViewDelegate argumentDelegate ) {
        delegate = argumentDelegate;
    }

    @Override
    public void setIds( long[] ids ) {
        pageList = new ArrayList< Integer >();
        List< TOCElement > toc = Kernel.getLocalProvider().getTableOfContentsInfo( ids[ 0 ] );

        if ( toc == null ) {
            Toast.makeText( getContext() , "There is no table of contents." ,
                    Toast.LENGTH_LONG ).show();            
            return;
        }

        for (TOCElement element : toc) {
            pageList.add( element.page );
            tocArrayAdapter.add( element.text );
        }

    }
}
