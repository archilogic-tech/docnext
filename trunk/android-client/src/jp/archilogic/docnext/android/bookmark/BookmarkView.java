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

public class BookmarkView extends LinearLayout implements CoreView {
    public class BookmarkImageAdapter extends BaseAdapter {
        public class Thumnail extends LinearLayout {

            public Thumnail( final Context context ) {
                super( context );
                LayoutInflater.from( context ).inflate( R.layout.bookmark_item , this , true );
            }
        }

        public long id = 0;
        protected int count = 0;
        private final List< Integer > bookmarkList;

        public BookmarkImageAdapter( final long aId ) {
            id = aId;

            bookmarkList = provider.getBookmarkInfo( aId );
        }

        private Bitmap getBitmap( final int page ) {
            InputStream in = null;
            Bitmap bitmap;
            try {
                final int level = 0;
                final int px = 0;
                final int py = 0;

                in = new FileInputStream( provider.getImagePath( id , page , level , px , py ) );
                bitmap = BitmapFactory.decodeStream( in );
            } catch ( final IOException e ) {
                throw new RuntimeException( e );
            } finally {
                IOUtils.closeQuietly( in );
            }
            return bitmap;
        }

        @Override
        public int getCount() {
            return bookmarkList.size();
        }

        @Override
        public Object getItem( final int position ) {
            return null;
        }

        @Override
        public long getItemId( final int position ) {
            return bookmarkList.get( position );
        }

        @Override
        public View getView( final int position , final View convertView , final ViewGroup parent ) {
            // ImageView imageView;
            //
            // if ( convertView == null ) {
            // imageView = new ImageView( getContext() );
            // imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
            // imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
            // imageView.setPadding( 20 , 20, 20, 20 );
            // } else {
            // imageView = (ImageView) convertView;
            // }

            // imageView.setImageBitmap( getBitmap( bookmarkList.get( position ) ) );

            LinearLayout view;

            if ( convertView == null ) {
                final ImageView imageView = new ImageView( getContext() );
                imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                imageView.setPadding( 20 , 20 , 20 , 20 );
                imageView.setImageBitmap( getBitmap( bookmarkList.get( position ) ) );

                view = new LinearLayout( getContext() );
                LayoutInflater.from( getContext() ).inflate( R.layout.bookmark_item , view , true );

                view.setLayoutParams( new GridView.LayoutParams( 250 , 250 ) );
                view.setPadding( 10 , 10 , 10 , 10 );
                view.addView( imageView );
            } else {
                view = ( LinearLayout ) convertView;
            }

            return view;
        }
    }

    private long id = -1;
    private GridView gridView;
    private Button button;
    private CoreViewDelegate delegate;

    private final LocalProvider provider;

    public BookmarkView( final Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.bookmark , this , true );
        setOrientation( VERTICAL );

        provider = Kernel.getLocalProvider();
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
        id = ids[ 0 ];

        final LocalPathManager localPathManager = new LocalPathManager();
        localPathManager.ensureBookmark( id );

        final TreeSet< Integer > bookmarkSet = new TreeSet< Integer >();
        bookmarkSet.addAll( provider.getBookmarkInfo( id ) );
        final List< Integer > bookmarkList = new ArrayList< Integer >();
        bookmarkList.addAll( bookmarkSet );
        provider.setBookmarkInfo( id , bookmarkList );

        gridView = ( GridView ) findViewById( R.id.bookmark );
        gridView.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick( final AdapterView< ? > parent , final View v , final int position ,
                    final long rowId ) {
                final Intent intent = new Intent();
                final int page = provider.getBookmarkInfo( id ).get( position );
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , page );
                delegate.changeCoreViewType( DocumentType.IMAGE , intent );
            }
        } );

        final BookmarkImageAdapter adapter = new BookmarkImageAdapter( id );
        gridView.setAdapter( adapter );

        button = ( Button ) findViewById( R.id.add_button );
        button.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                final TreeSet< Integer > bookmarkSet = new TreeSet< Integer >();
                bookmarkSet.addAll( provider.getBookmarkInfo( id ) );
                final List< Integer > bookmarkList = new ArrayList< Integer >();
                bookmarkList.addAll( bookmarkSet );

                // just for the test
                // it does not work if page beyond 10
                final Double doublePage = Math.random() * 10 % 10;
                final int page = doublePage.intValue();
                bookmarkList.add( page );
                provider.setBookmarkInfo( id , bookmarkList );

                final BookmarkImageAdapter adapter = new BookmarkImageAdapter( id );
                gridView.setAdapter( adapter );
            }
        } );
    }
}
