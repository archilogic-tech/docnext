package jp.archilogic.docnext.android.bookmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.archilogic.docnext.android.Kernel;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class BookmarkImageAdapter extends BaseAdapter {
    public long id = 0;
    protected int count = 0;
    private Context mContext;
    private List< Integer > bookmarkList;

    public BookmarkImageAdapter( Context c, long aId ) {
        mContext = c;
        id = aId;

        bookmarkList = Kernel.getLocalProvider().getBookmarkInfo( aId );
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
        ImageView imageView;
        
        if ( convertView == null ) {
            imageView = new ImageView( mContext );
            imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
            imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
            imageView.setPadding( 20 , 20, 20, 20 );
        } else {
            imageView = (ImageView) convertView;
        }
        
        imageView.setImageBitmap( getBitmap( bookmarkList.get( position ) ) );
        return imageView;
    }
    
    private Bitmap getBitmap( int page ) {
        InputStream in = null;
        Bitmap bitmap;
        try {
            int level = 0;
            int px = 0;
            int py = 0;

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
