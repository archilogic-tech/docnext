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
    public class ThumnailImageAdapter extends BaseAdapter {
        public long id = 0;
        protected int count = 0;

        public ThumnailImageAdapter( final long aId ) {
            id = aId;

            final DocInfo doc = Kernel.getLocalProvider().getDocInfo( id );
            count = doc.pages;
        }

        private Bitmap getBitmap( final int page ) {
            InputStream in = null;
            Bitmap bitmap;
            try {
                final int level = 0;
                final int px = 0;
                final int py = 0;

                // unsure whether getImagePath is correct method for thumnail.
                in = new FileInputStream( Kernel.getLocalProvider().getImagePath( id , page , level , px , py ) );

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
            return count;
        }

        @Override
        public Object getItem( final int position ) {
            return null;
        }

        @Override
        public long getItemId( final int position ) {
            return 0;
        }

        @Override
        public View getView( final int position , final View convertView , final ViewGroup parent ) {
            ImageView imageView;

            if ( convertView == null ) {
                imageView = new ImageView( getContext() );
                imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                imageView.setPadding( 8 , 8 , 8 , 8 );
            } else {
                imageView = ( ImageView ) convertView;
            }

            imageView.setImageBitmap( getBitmap( position ) );
            return imageView;
        }

        public void setCount( final int aCount ) {
            count = aCount;
        }
    }

    private final GridView gridView;

    private CoreViewDelegate delegate;

    public ThumnailView( final Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.thumnail , this , true );

        gridView = ( GridView ) findViewById( R.id.thumnail );
        gridView.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick( final AdapterView< ? > parent , final View v , final int position , final long id ) {
                final Intent intent = new Intent();
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , position );
                delegate.changeCoreViewType( DocumentType.IMAGE , intent );
            }
        } );
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
        final GridView gridview = ( GridView ) findViewById( R.id.thumnail );
        final ThumnailImageAdapter adapter = new ThumnailImageAdapter( ids[ 0 ] );
        gridview.setAdapter( adapter );
    }
}
