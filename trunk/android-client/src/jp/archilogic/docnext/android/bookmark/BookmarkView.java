package jp.archilogic.docnext.android.bookmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.provider.local.LocalPathManager;
import jp.archilogic.docnext.android.provider.local.LocalProvider;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class BookmarkView  extends LinearLayout implements CoreView {
    private long id = -1;
    private GridView gridView;
    private Button button;
    private CoreViewDelegate delegate;
    private LocalProvider provider;

    public BookmarkView( Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.bookmark , this , true );
        setOrientation( VERTICAL );
        
        provider = Kernel.getLocalProvider();
    }
    
    @Override
    public void onDoubleTapGesture( PointF point ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDragGesture( PointF delta ) {
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
    public void onTapGesture( PointF point ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onZoomGesture( float scaleDelta, PointF center ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDelegate( CoreViewDelegate argumentDelegate ) {
        delegate = argumentDelegate;
    }

    @Override
    public void setIds( long[] ids ) {
        id = ids[ 0 ];
        
        LocalPathManager localPathManager = new LocalPathManager();
        localPathManager.ensureBookmark( id );
        
        TreeSet< Integer > bookmarkSet = new TreeSet< Integer >(); 
        bookmarkSet.addAll( provider.getBookmarkInfo( id ) );
        List< Integer > bookmarkList = new ArrayList<Integer>();
        bookmarkList.addAll( bookmarkSet );
        provider.setBookmarkInfo( id, bookmarkList );
        
        gridView = (GridView) findViewById( R.id.bookmark );
        gridView.setOnItemClickListener( new OnItemClickListener() {
            public void onItemClick( AdapterView< ? > parent, View v, int position, long rowId ) {
                Intent intent = new Intent();
                int page = provider.getBookmarkInfo( id ).get( position );
                intent.putExtra( CoreViewActivity.EXTRA_PAGE, page );
                delegate.changeCoreViewType( DocumentType.IMAGE, intent );
            }
        });
        
        BookmarkImageAdapter adapter = new BookmarkImageAdapter( id );
        gridView.setAdapter( adapter );
        
        button = (Button) findViewById( R.id.add_button );
        button.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                TreeSet< Integer > bookmarkSet = new TreeSet< Integer >(); 
                bookmarkSet.addAll( provider.getBookmarkInfo( id ) );
                List< Integer > bookmarkList = new ArrayList<Integer>();
                bookmarkList.addAll( bookmarkSet );
                
                // just for the test
                // it does not work if page beyond 10
                Double doublePage = ( Math.random() * 10 ) % 10;
                int page = doublePage.intValue();
                bookmarkList.add( page );
                provider.setBookmarkInfo( id, bookmarkList );
                
                BookmarkImageAdapter adapter = new BookmarkImageAdapter( id );
                gridView.setAdapter( adapter );
            }
        } );
    }
    
    public class BookmarkImageAdapter extends BaseAdapter {
        public class Thumnail extends LinearLayout {

            public Thumnail( Context context ) {
                super( context );
                LayoutInflater.from( context ).inflate( R.layout.bookmark_item , this , true );
           }
        }
        
        public long id = 0;
        protected int count = 0;
        private List< Integer > bookmarkList;

        public BookmarkImageAdapter( long aId ) {
            id = aId;

            bookmarkList = provider.getBookmarkInfo( aId );
        }
        
        public int getCount() {
            return bookmarkList.size();
        }
        
        public Object getItem( int position ) {
            return null;
        }
        
        public long getItemId( int position ) {
            return bookmarkList.get( position );
        }
        
        public View getView( int position, View convertView, ViewGroup parent ) {
//            ImageView imageView;
//            
//            if ( convertView == null ) {
//          imageView = new ImageView( getContext() );
//          imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
//          imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
//          imageView.setPadding( 20 , 20, 20, 20 );
//            } else {
//                imageView = (ImageView) convertView;
//            }
            
//          imageView.setImageBitmap( getBitmap( bookmarkList.get( position ) ) );
            
            LinearLayout view;
            
            if (convertView == null) {
                ImageView imageView = new ImageView( getContext() );
                imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                imageView.setPadding( 20 , 20, 20, 20 );
                imageView.setImageBitmap( getBitmap( bookmarkList.get( position ) ) );

                view = new LinearLayout( getContext() );
                LayoutInflater.from( getContext() ).inflate( R.layout.bookmark_item , view , true );

                view.setLayoutParams( new GridView.LayoutParams( 250 , 250 ) );
                view.setPadding( 10 , 10 , 10 , 10 );
                view.addView( imageView );
            } else {
                view = (LinearLayout) convertView;
            }
            
            return view;
        }
        
        private Bitmap getBitmap( int page ) {
            InputStream in = null;
            Bitmap bitmap;
            try {
                int level = 0;
                int px = 0;
                int py = 0;

                in = new FileInputStream( provider.getImagePath( id , page , level , px , py ) );
                bitmap =  BitmapFactory.decodeStream( in );
            } catch ( final IOException e ) {
                throw new RuntimeException( e );
            } finally {
                IOUtils.closeQuietly( in );
            }
            return bitmap;
        }
    }

}
