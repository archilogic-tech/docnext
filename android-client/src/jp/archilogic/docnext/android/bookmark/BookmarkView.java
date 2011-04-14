package jp.archilogic.docnext.android.bookmark;

import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.NavigationView;
import jp.archilogic.docnext.android.info.BookmarkInfo;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkView extends NavigationView implements HasPage {
    private final String TAG = BookmarkView.class.getName();
    
    private ListView _listView;
    private ArrayAdapter< BookmarkInfo > _adapter;

    private LinearLayout _addButton;

    public BookmarkView( final Context context ) {
        super( context );
        
        Log.d( TAG , "BookmarkView" );
    }
    
    private ArrayAdapter< BookmarkInfo > buildAdapter() {
        return new ArrayAdapter< BookmarkInfo >( getContext(), R.layout.navigation_textview ) {
            public View getView( final int position , View convertView , final ViewGroup parent ) {
                return buildRow( getItem( position ) );
            }
        };
    }

    private View buildAddButton() {
        LinearLayout layout = new LinearLayout( getContext() );
        layout.setGravity( Gravity.CENTER );

        layout.setOnClickListener( buildAddButtonClickListener() );
        
        ImageView image = new ImageView( getContext() );
        image.setImageResource( R.drawable.button_add );
        image.setLayoutParams( new LinearLayout.LayoutParams(
                dp( 30 ) , dp( 30 ) ) );
        layout.addView( image );
        
        TextView textView = new TextView( getContext() );
        textView.setText( "add a bookmark" );
        textView.setTextSize( TypedValue.COMPLEX_UNIT_SP , 16 );
        layout.addView( textView );

        _addButton = layout;
        setButtonAvailability();
        return layout;
    }
    
    private OnClickListener buildAddButtonClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick( View v ) {
                BookmarkInfo bookmark = new BookmarkInfo( _page );
                List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
                if ( !bookmarks.contains( bookmark ) ) { 
                    bookmarks.add( bookmark );
                    Kernel.getLocalProvider().setBookmarkInfo( _id , bookmarks );
                    bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
                    _adapter.clear();
                    for ( BookmarkInfo element : bookmarks ) {
                        _adapter.add( element );
                    }
                }
                setButtonAvailability();
            }
        };
    }
    
    private View buildRow( final BookmarkInfo bookmark ) {
        LinearLayout layout = new LinearLayout( getContext() );
        layout.setOrientation( LinearLayout.HORIZONTAL );
        
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick( View view ) {
                goTo( bookmark.page );
            }};
        LinearLayout.LayoutParams textLayout = new LinearLayout.LayoutParams( 
                LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT ); 
        
        TextView textView = new TextView( getContext() );
        textView.setText( bookmark.text );
        textView.setLayoutParams( textLayout );
        textView.setOnClickListener( clickListener );
        layout.addView( textView );

        TextView page = new TextView( getContext() );
        page.setText( String.valueOf( bookmark.page ) );
        textView.setTextSize( TypedValue.COMPLEX_UNIT_SP , 16 );
        page.setLayoutParams( new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT , 1 ) );
        page.setPadding( 0 , 0 , dp( 50 ) , 0 );
        page.setGravity( Gravity.RIGHT );
        page.setOnClickListener( clickListener );
        layout.addView( page );
        
        ImageView deleteButton = new ImageView( getContext() );
        deleteButton.setLayoutParams( new LinearLayout.LayoutParams(
                dp( 30 ) , dp( 30 ) ) );
        deleteButton.setImageResource( R.drawable.button_delete );
        deleteButton.setScaleType( ScaleType.FIT_CENTER );
        deleteButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( View v ) {
                List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
                bookmarks.remove( bookmark );
                Kernel.getLocalProvider().setBookmarkInfo( _id , bookmarks );

                _adapter.remove( bookmark );
                setButtonAvailability();
            }
        });
        layout.addView( deleteButton );
        
        return layout;
    }

    private int dp( final float value ) {
        final float density = getResources().getDisplayMetrics().density;

        return Math.round( value * density );
    }
 
    public void init() {
        addView( buildAddButton() );
        initListView();
    }

    private void initListView() {
        List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
        if ( bookmarks == null ) {
            Toast.makeText( getContext() , R.string.empty_bookmark , Toast.LENGTH_LONG ).show();
            _delegate.back();
            return;
        }

        _listView = new ListView( getContext() );
        _adapter = buildAdapter(); 
        for ( BookmarkInfo bookmark : bookmarks ) {
            _adapter.add( bookmark );
        }
        _listView.setAdapter( _adapter );
        addView( _listView );
    }
    
    private void setButtonAvailability() {
        _addButton.setEnabled( true );
        List< BookmarkInfo > bookmarks = Kernel.getLocalProvider().getBookmarkInfo( _id );
        for ( BookmarkInfo bookmark : bookmarks ) {
            if ( _page == bookmark.page ) {
                _addButton.setEnabled( false );
            }
        }
    }
}
