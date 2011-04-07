package jp.archilogic.docnext.android.toc;

import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.TOCElement;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.Receiver;
import jp.archilogic.docnext.android.type.TaskErrorType;
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
    private ArrayAdapter< String > tocArrayAdapter;
    private List< TOCElement > tocList;

    private OnItemClickListener mTcoClickListener = new OnItemClickListener() {
        public void onItemClick( AdapterView< ? > av , View v , int arg2 , long arg3 ) {
            Intent intent = new Intent();
            intent.putExtra( CoreViewActivity.EXTRA_PAGE , tocList.get( arg2 ).page );
            delegate.changeCoreViewType( DocumentType.IMAGE , intent );
        }
    };
    private CoreViewDelegate delegate;
    private long id;
    private Context _context;

    public TOCView(Context context) {
        super( context );

        _context = context;
        LayoutInflater.from( context ).inflate( R.layout.toc , this , true );
        
        ListView tocListView = (ListView) findViewById( R.id.table_of_contents_listview );
        tocArrayAdapter = new ArrayAdapter< String >( getContext() , R.layout.toc_title );
        tocListView.setAdapter( tocArrayAdapter );
        tocListView.setOnItemClickListener( mTcoClickListener );
    }

    @Override
    public void onFlingGesture( PointF velocity ) {
    }
    @Override
    public void onDoubleTapGesture( PointF point ) {
    }
    @Override
    public void onDragGesture( PointF delta ) {
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
    @Override
    public void onTapGesture( PointF point ) {
    }
    @Override
    public void onZoomGesture( float scaleDelta , PointF center ) {
    }

    public void setDelegate( CoreViewDelegate argumentDelegate ) {
        delegate = argumentDelegate;
    }

    private class DownloadReceiver implements Receiver< Void > {
        @Override
        public void error( final TaskErrorType error ) {
            Toast.makeText( getContext() , "There is no table of contents." , Toast.LENGTH_LONG ).show();
        }

        @Override
        public void receive( final Void result ) {
            List< TOCElement > tocList = Kernel.getLocalProvider().getTableOfContentsInfo( id );
            
            for ( TOCElement element : tocList ) {
                tocArrayAdapter.add( element.text );
            }
        }
    }

    @Override
    public void setIds( long[] ids ) {
        id = ids[ 0 ];
        setTOC();
    }
    
    private void setTOC() {
        tocList = Kernel.getLocalProvider().getTableOfContentsInfo( id );

        if ( tocList == null ) {
            Receiver< Void > receiver = new DownloadReceiver();
            DownloadTask task = Kernel.getRemoteProvider().getTableOfContentsInfo( _context.getApplicationContext() , receiver , id );
            task.execute();
        } else {
            for ( TOCElement element : tocList ) {
                tocArrayAdapter.add( element.text );
            }
        }
    }
}