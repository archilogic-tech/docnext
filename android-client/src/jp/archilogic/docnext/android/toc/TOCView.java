package jp.archilogic.docnext.android.toc;

import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.NavigationView;
import jp.archilogic.docnext.android.info.TOCElement;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.Receiver;
import jp.archilogic.docnext.android.type.TaskErrorType;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TOCView extends NavigationView {
    private class DownloadReceiver implements Receiver< Void > {
        @Override
        public void error( final TaskErrorType error ) {
            Toast.makeText( getContext() , "There is no table of contents." , Toast.LENGTH_LONG )
                    .show();
        }

        @Override
        public void receive( final Void result ) {
            initListView();
        }
    }

    private final OnItemClickListener _itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick( final AdapterView< ? > av , final View v , final int arg2 ,
                final long arg3 ) {
            final Intent intent = new Intent();
            int page = Kernel.getLocalProvider().getTableOfContentsInfo(_id ).get( arg2 ).page; 
            if ( !Kernel.getLocalProvider().isAllImageExists( _id , page ) ) {
                return;
            }
            intent.putExtra( CoreViewActivity.EXTRA_PAGE , page );
            _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
        }
    };

    public TOCView( final Context context ) {
        super( context );
    }

    private void ensureTOC() {
        if ( Kernel.getLocalProvider().getTableOfContentsInfo( _id ) == null ) {
            final Receiver< Void > receiver = new DownloadReceiver();
            final DownloadTask task =
                    Kernel.getRemoteProvider().getTableOfContentsInfo(
                            getContext().getApplicationContext() , receiver , _id );
            task.execute();
        }
    }
    
    public void init() {
        ensureTOC();
        initListView();
    }

    public void initListView() {
        List< TOCElement >toc = Kernel.getLocalProvider().getTableOfContentsInfo( _id );
        
        if ( toc == null ) {
            return;
        }
        
        ListView listView = new ListView( getContext() );
        ArrayAdapter < TOCElement > adapter  = new ArrayAdapter< TOCElement >( getContext() , R.layout.navigation_textview ) {
          @Override
          public View getView(int position , View convertView ,  ViewGroup parent ) {
              LinearLayout layout;
              layout = new LinearLayout( getContext() );
              
              layout.setOrientation( LinearLayout.HORIZONTAL );

              TextView textView = new TextView( getContext() );
              textView.setText( getItem( position ).text );
              textView.setTextSize( TypedValue.COMPLEX_UNIT_SP , 16 );
              textView.setLayoutParams( new LinearLayout.LayoutParams( 
                      LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT ) );
              layout.addView( textView );
              
              TextView page = new TextView( getContext() );
              page.setText( String.valueOf( getItem( position ).page ) );
              textView.setTextSize( TypedValue.COMPLEX_UNIT_SP , 16 );
              page.setLayoutParams( new LinearLayout.LayoutParams( 
                      LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT , 1 ) );
              page.setPadding( 100 , 0 , 50 , 0 );
              page.setGravity( Gravity.RIGHT );
              layout.addView( page );
              Log.d( "hoge" , "height: " + layout.getHeight() + " width:" + layout.getWidth() );
              
              return layout;
          }
        };
        for ( TOCElement element : toc ) {
            adapter.add( element );
        }
        listView.setAdapter( adapter );
        listView.setOnItemClickListener( _itemClickListener );
        addView( listView );
    }
}
