package jp.archilogic.docnext.android.thumnail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.provider.local.LocalPathManager;
import jp.archilogic.docnext.android.provider.local.LocalProvider;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
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
        private int count = 0;
        private final LocalProvider provider = Kernel.getLocalProvider();
        private final LocalPathManager pathManager = new LocalPathManager();
        private final String TAG = "Thumnail";

        public ThumnailImageAdapter( final long aId ) {
            id = aId;

            final DocInfo doc = provider.getDocInfo( id );
            count = doc.pages;
        }

        private Bitmap getBitmap( final int page ) {
            final String thumnailPath = pathManager.getThumnailPath( id , page );
            final File thumnailFile = new File( thumnailPath );
            if ( thumnailFile.exists() ) {
                final Bitmap thumnailBitmap = BitmapFactory.decodeFile( thumnailPath );
                return thumnailBitmap;
            }

            final ImageInfo imageInfo = provider.getImageInfo( id );
            final int height = imageInfo.height;
            final int width = imageInfo.width;
            final Bitmap bigBitmap = Bitmap.createBitmap( width , height , Bitmap.Config.RGB_565 );
            final Canvas canvas = new Canvas( bigBitmap );

            // unsure whether getImagePath is correct method for thumnail.
            InputStream in = null;
            try {
                for ( int x = 0 ; x < Math.ceil( width / 512. ) ; x++ ) {
                    for ( int y = 0 ; y < Math.ceil( height / 512. ) ; y++ ) {
                        final int level = 0;
                        Log.d( TAG , String.format( "id:%d, page:%d x:%d y:%d" , id , page , x , y ) );
                        final String path = provider.getImagePath( id , page , level , x , y );
                        Log.d( "thumnail" , "path: " + path );
                        if ( path == null ) {
                            break;
                        }
                        in = new FileInputStream( path );
                        final Bitmap bitmap = BitmapFactory.decodeStream( in );
                        canvas.drawBitmap( bitmap , 512 * x , 512 * y , null );
                    }
                }
            } catch ( final IOException e ) {
                throw new RuntimeException( e );
            } finally {
                IOUtils.closeQuietly( in );
            }

            final Bitmap resizedBitmap = getResizedBitmap( bigBitmap , 176 , 256 );
            try {
                final File file = new File( thumnailPath );
                final FileOutputStream out = new FileOutputStream( file );
                resizedBitmap.compress( Bitmap.CompressFormat.JPEG , 90 , out );
            } catch ( final Exception exception ) {
                Log.e( TAG , exception.getMessage() );
            }
            return resizedBitmap;
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

        public Bitmap getResizedBitmap( final Bitmap bitmap , final int newWidth ,
                final int newHeight ) {

            final int width = bitmap.getWidth();
            final int height = bitmap.getHeight();

            final float scaleWidth = ( float ) newWidth / width;
            final float scaleHeight = ( float ) newHeight / height;

            final Matrix matrix = new Matrix();

            matrix.postScale( scaleWidth , scaleHeight );

            final Bitmap resizedBitmap =
                    Bitmap.createBitmap( bitmap , 0 , 0 , width , height , matrix , false );

            return resizedBitmap;
        }

        @Override
        public View getView( final int position , final View convertView , final ViewGroup parent ) {
            ImageView imageView;

            if ( convertView == null ) {
                imageView = new ImageView( getContext() );
                // imageView.setLayoutParams( new GridView.LayoutParams( dp( 80
                // ) , dp( 90 ) ) );
                imageView.setScaleType( ImageView.ScaleType.FIT_CENTER );
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

    private long id = 0;

    private final GridView gridView = ( GridView ) findViewById( R.id.thumnail );
    private CoreViewDelegate delegate;

    public ThumnailView( final Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.thumnail , this , true );
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
    }

    @Override
    public void onDragGesture( final PointF delta ) {
    }

    @Override
    public void onFlingGesture( final PointF velocity ) {
    }

    @Override
    public void onGestureBegin() {
    }

    @Override
    public void onGestureEnd() {
    }

    @Override
    public void onMenuVisibilityChange( final boolean isMenuVisible ) {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onTapGesture( final PointF point ) {
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
    }

    @Override
    public void setDelegate( final CoreViewDelegate argumentDelegate ) {
        delegate = argumentDelegate;
    }

    private void setGridView() {
        final GridView gridview = ( GridView ) findViewById( R.id.thumnail );
        gridview.setAdapter( new ThumnailImageAdapter( id ) );

        gridView.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick( final AdapterView< ? > parent , final View v ,
                    final int position , final long id ) {
                final Intent intent = new Intent();
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , position );
                delegate.changeCoreViewType( DocumentType.IMAGE , intent );
            }
        } );
    }

    @Override
    public void setIds( final long[] ids ) {
        setGridView();
    }
}
