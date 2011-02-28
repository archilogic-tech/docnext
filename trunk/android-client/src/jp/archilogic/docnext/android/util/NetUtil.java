package jp.archilogic.docnext.android.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class NetUtil {
    private static final int TIMEOUT = 10 * 1000;

    public static HttpResponse asResponse( final HttpUriRequest req ) throws IOException {
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout( params , TIMEOUT );
        HttpConnectionParams.setSoTimeout( params , TIMEOUT );
        HttpClientParams.setRedirecting( params , true );

        return new DefaultHttpClient( params ).execute( req );
    }

    public static InputStream asStream( final HttpResponse res ) throws IOException {
        final int statusCode = res.getStatusLine().getStatusCode();
        if ( statusCode < 400 ) {
            return decompress( res.getEntity() );
        } else {
            throw new IOException( "Invalid statusCode: " + statusCode );
        }
    }

    private static InputStream decompress( final HttpEntity entity ) throws IOException {
        final Header encodingHeader = entity.getContentEncoding();

        if ( encodingHeader != null ) {
            final String encoding = encodingHeader.getValue().toLowerCase();

            if ( encoding.equals( "gzip" ) ) {
                return new GzipDecompressingEntity( entity ).getContent();
            } else if ( encoding.equals( "deflate" ) ) {
                return new DeflateDecompressingEntity( entity ).getContent();
            }
        }

        return entity.getContent();
    }

    public static InputStream getFont( final String name ) throws IOException {
        final HttpGet get = new HttpGet( String.format( "http://twist.archilogic.jp/_docnext/font/%s" , name ) );

        return asStream( asResponse( get ) );
    }

    public static InputStream getImagePage( final long id , final int page ) throws IOException {
        final HttpGet get = new HttpGet( String.format( "http://twist.archilogic.jp/_docnext/comic-%03d.jpg" , page ) );

        return asStream( asResponse( get ) );
    }

    public static InputStream getImagePageThumbnail( final long id , final int page ) throws IOException {
        final HttpGet get =
                new HttpGet( String.format( "http://twist.archilogic.jp/_docnext/comic-thumb-%03d.jpg" , page ) );

        return asStream( asResponse( get ) );
    }
}
