package jp.archilogic.docnext.android.bookmark;

import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.NavigationView;
import jp.archilogic.docnext.android.info.BookmarkInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkView extends NavigationView implements HasPage {
    private final String TAG = BookmarkView.class.getName();
    
    private ListView _listView;
    private ArrayAdapter< BookmarkInfo > _adapter;
    private int _page;

    public BookmarkView(Context context) {
        super( context );
        
        setOrientation( VERTICAL );
        Log.d( TAG , "BookmarkView" );
    }
    
    private ArrayAdapter< BookmarkInfo > getAdapter() {
        return new ArrayAdapter< BookmarkInfo >( getContext(), R.layout.navigation_textview ) {
            public View getView( final int position , View convertView , final ViewGroup parent ) {
                OnClickListener clickListener = new OnClickListener() {
                    @Override
                    public void onClick( View view ) {
                        List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
                        final Intent intent = new Intent();
                        int page = bookmarks.get( position ).page;
                        intent.putExtra( CoreViewActivity.EXTRA_PAGE , page );
                        _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
                    }
                };
                
                LinearLayout layout = new LinearLayout( getContext() );
                layout.setOrientation( LinearLayout.HORIZONTAL );
                
                TextView textView = new TextView( getContext() );
                textView.setText( getItem( position ).text );
                textView.setLayoutParams( new LinearLayout.LayoutParams( 
                        LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT ) );
                textView.setOnClickListener( clickListener );
                layout.addView( textView );

                TextView page = new TextView( getContext() );
                page.setText( String.valueOf( getItem( position ).page ) );
                textView.setTextSize( TypedValue.COMPLEX_UNIT_SP , 16 );
                page.setLayoutParams( new LinearLayout.LayoutParams( 
                        LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT , 1 ) );
                page.setPadding( 0 , 0 , 50 , 0 );
                page.setGravity( Gravity.RIGHT );
                page.setOnClickListener( clickListener );
                layout.addView( page );
                
                Button deleteButton = new Button( getContext() );
                deleteButton.setText( "X" );
                deleteButton.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
                        bookmarks.remove( getItem( position ) );
                        Kernel.getLocalProvider().setBookmarkInfo( _id , bookmarks );

                        _adapter.remove( getItem( position ) );
                    }
                });
                deleteButton.setLayoutParams( new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT ));
                deleteButton.setGravity( Gravity.RIGHT );
                layout.addView( deleteButton );
                Log.d( "hoge" , "height: " + layout.getHeight() + " width:" + layout.getWidth() );
                return layout;
            }
        };
    }
 
    @Override
    public int getPage() {
        return _page;
    }
    
    public void init() {
        initAddButton();
        initListView();
    }

    private void initAddButton() {
        final Button button = new Button( getContext() );
        
        button.setText( "add bookmark" );
        if ( Kernel.getLocalProvider().getBookmarkInfo( _id ).contains( new BookmarkInfo( _page ) ) ) {
            button.setEnabled( false );
        }
        button.setOnClickListener( new OnClickListener(){
            @Override
            public void onClick( View v ) {
                BookmarkInfo bookmark = new BookmarkInfo( _page );
                List< BookmarkInfo > bookmarks =Kernel.getLocalProvider().getBookmarkInfo( _id );
                if ( !bookmarks.contains( bookmark ) ) { 
                    //_adapter.add( bookmark );
                    bookmarks.add( bookmark );
                    Kernel.getLocalProvider().setBookmarkInfo( _id , bookmarks );
                    bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
                    _adapter.clear();
                    for ( BookmarkInfo element : bookmarks ) {
                        _adapter.add( element );
                    }
                }
                button.setEnabled( false );
            }});
        addView( button );
    }
    
    private void initListView() {
        List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
        if ( bookmarks == null ) {
            Toast.makeText( getContext() , R.string.empty_bookmark , Toast.LENGTH_LONG ).show();
            _delegate.back();
            return;
        }

        _listView = new ListView( getContext() );
        _adapter = getAdapter(); 
        for ( BookmarkInfo bookmark : bookmarks ) {
            _adapter.add( bookmark );
        }
        _listView.setAdapter( _adapter );
        addView( _listView );
    }

    @Override
    public void setPage( int page ) {
        _page = page;
    }
}
