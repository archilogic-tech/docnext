package jp.archilogic.docnext.android.toc;

import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.NavigationView;
import jp.archilogic.docnext.android.info.TOCElement;
import android.content.Context;
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

public class TOCView extends NavigationView {

    private final OnItemClickListener _itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick( final AdapterView< ? > av , final View v , final int arg2 ,
                final long arg3 ) {
            int page = Kernel.getLocalProvider().getTableOfContentsInfo(_id ).get( arg2 ).page;
            goTo( page );
        }
    };

    public TOCView( final Context context ) {
        super( context );
    }

    public void init() {
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
