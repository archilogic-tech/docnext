package jp.archilogic.docnext.android.thumnail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.DocumentType;

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
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

public class ThumnailView extends FrameLayout implements CoreView {

    private GridView gridView;
    private CoreViewDelegate delegate;

    public ThumnailView( Context context ) {
        super( context );
        
        LayoutInflater.from( context ).inflate( R.layout.thumnail , this , true );
        
        gridView = (GridView)findViewById( R.id.thumnail );
        gridView.setOnItemClickListener( new OnItemClickListener() {
            public void onItemClick( AdapterView< ? > parent, View v, int position, long id ) {
                Intent intent = new Intent();
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , position );
                delegate.changeCoreViewType( DocumentType.IMAGE, intent );
            }
        });
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
        GridView gridview = ( GridView ) findViewById( R.id.thumnail );
        ThumnailImageAdapter adapter = new ThumnailImageAdapter( ids[0] );
        gridview.setAdapter( adapter );
    }
    
    public class ThumnailImageAdapter extends BaseAdapter {
        public long id = 0;
        protected int count = 0;

        public ThumnailImageAdapter( long aId ) {
            id = aId;
            
            final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );
            count = doc.pages;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount( int aCount ) {
            count = aCount;
        }
        
        public Object getItem( int position ) {
            return null;
        }
        
        public long getItemId( int position ) {
            return 0;
        }
        
        public View getView( int position, View convertView, ViewGroup parent ) {
            ImageView imageView;
            
            if ( convertView == null ) {
                imageView = new ImageView( getContext() );
                imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                imageView.setPadding( 8 , 8 , 8 , 8 );
            } else {
                imageView = (ImageView) convertView;
            }
            
            imageView.setImageBitmap( getBitmap( position ) );
            return imageView;
        }
        
        private Bitmap getBitmap( int page ) {
            InputStream in = null;
            Bitmap bitmap;
            try {
                int level = 0;
                int px = 0;
                int py = 0;

                // unsure whether getImagePath is correct method for thumnail.
                in = new FileInputStream( 
                        Kernel.getLocalProvider().getImagePath( id , page , level , px , py ) );

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
