package jp.archilogic.docnext.android.thumnail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.info.DocInfo;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ThumnailImageAdapter extends BaseAdapter {
    public long id = 0;
    protected int count = 0;
    private Context mContext;

    public ThumnailImageAdapter( Context c, long aId ) {
        mContext = c;
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
            imageView = new ImageView( mContext );
            imageView.setLayoutParams( new GridView.LayoutParams( 180 , 180 ) );
            imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
            imageView.setPadding( 8 , 8, 8, 8 );
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
