package jp.archilogic.docnext.android.thumbnail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.provider.local.LocalPathManager;
import jp.archilogic.docnext.android.provider.local.LocalProvider;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ThumbnailImageAdapter extends BaseAdapter {
    private long _id;
    private Context _context;
    private int count = 0;
    private LocalProvider provider = Kernel.getLocalProvider();
    private LocalPathManager pathManager = new LocalPathManager();
    private ImageInfo _imageInfo;
    private String TAG = "ThumnailImageAdapter";
    private Direction _direction = Direction.RIGHT;

    public enum Direction {
        LEFT , RIGHT;
    }
    
    public ThumbnailImageAdapter( Context count , final long id ) {
        this( count , id , Direction.RIGHT );
    }
    
    public ThumbnailImageAdapter( Context context , final long id , Direction direction ) {
        _id = id;
        _context = context;
        _direction = direction;

        final DocInfo doc = provider.getDocInfo( _id );
        _imageInfo = provider.getImageInfo( _id );
        count = doc.pages;
    }

    private Bitmap composeBitmap( final int page ) {
        // unsure whether getImagePath is correct method for thumbnail.
        Bitmap bitmap = Bitmap.createBitmap( _imageInfo.width , _imageInfo.height , Bitmap.Config.RGB_565 );
        Canvas canvas = new Canvas( bitmap );
        InputStream in = null;
        for ( int x = 0; x < Math.ceil( _imageInfo.width / 512. ); x++ ) {
            for ( int y = 0; y < Math.ceil( _imageInfo.height / 512. ); y++ ) {
                final int level = 0;
                Log.d( TAG , String.format( "id:%d, page:%d x:%d y:%d" , _id , page , x , y ) );
                String path = provider.getImagePath( _id , page , level , x , y );
                Log.d( "thumnail" , "path: " + path );
                if ( path == null ) {
                    return null;
                }
                if ( !( new File( path )).exists() ) {
                    return null;
                }
                try {
                    in = new FileInputStream( path );
                    Bitmap textureBitmap = BitmapFactory.decodeStream( in );
                    canvas.drawBitmap( textureBitmap , 512 * x , 512 * y , null );
                } catch ( final IOException e ) {
                    throw new RuntimeException( e );
                } finally {
                    IOUtils.closeQuietly( in );
                }
            }
        }

        return bitmap;
    }
    
    private Bitmap getBitmap( final int page ) {
        String thumnailPath = pathManager.getThumbnailPath( _id , page );
        if ( ( new File( thumnailPath) ).exists() ) {
            return BitmapFactory.decodeFile( thumnailPath );
        }

        Bitmap bitmap = composeBitmap( page );
        if ( bitmap == null ) {
            return null;
        }
        
        WindowManager windowManager = (WindowManager)_context.getSystemService(Context.WINDOW_SERVICE);
        int displayWidth = windowManager.getDefaultDisplay().getWidth();
        int displayHeight = windowManager.getDefaultDisplay().getHeight();
        
        Bitmap resizedBitmap = getResizedBitmap( bitmap , displayWidth , displayHeight - 100 );
        bitmap.recycle();

        saveBitmap( resizedBitmap , thumnailPath );

        return resizedBitmap;
    }

    public Bitmap getResizedBitmap( Bitmap bitmap , int newWidth , int newHeight ) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ( (float) newWidth ) / width;
        float scaleHeight = ( (float) newHeight ) / height;

        Matrix matrix = new Matrix();

        matrix.postScale( scaleWidth , scaleHeight );

        Bitmap resizedBitmap = Bitmap.createBitmap( bitmap , 0 , 0 , width , height , matrix ,
                false );

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

    @Override
    public View getView( final int position , final View convertView , final ViewGroup parent ) {
        ImageView imageView;

        if ( convertView == null ) {
            imageView = new ImageView( _context );
            imageView.setScaleType( ImageView.ScaleType.FIT_CENTER );
        } else {
            imageView = (ImageView) convertView;
        }

        if ( _direction == Direction.RIGHT ) {
            imageView.setImageBitmap( getBitmap( position ) );
        } else {
            imageView.setImageBitmap( getBitmap( getCount() - position - 1 ) );
        }

        imageView.setLayoutParams( new Gallery.LayoutParams( 200 , 256 ) );
        return imageView;
    }

    public void setCount( final int aCount ) {
        count = aCount;
    }
    
    private void saveBitmap( Bitmap bitmap , String path ) {
        if ( bitmap == null ) {
            Log.e( TAG , "saveBitmap: bitmap is null" );
            return;
        }
        
        try {
            File file = new File( path );
            FileOutputStream out = new FileOutputStream( file );
            bitmap.compress( Bitmap.CompressFormat.JPEG , 90 , out );
        } catch ( Exception exception ) {
            Log.e( TAG , exception.getMessage() );
        }
    }
}
