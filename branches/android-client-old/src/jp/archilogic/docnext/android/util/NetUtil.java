package jp.archilogic.docnext.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.common.collect.Maps;

public class NetUtil {
    private static final int TIMEOUT = 10 * 1000;

    private static HttpGet buildGet( final String uri ) {
        final HttpGet ret = new HttpGet( uri );
        ret.setHeader( "Accept-Encoding" , "gzip,deflate,sdch" );

        return ret;
    }

    private static String buildParam( final Map< String , String > params ) {
        final StringBuilder ret = new StringBuilder();

        boolean first = true;
        for ( final Entry< String , String > param : params.entrySet() ) {
            if ( first ) {
                ret.append( "?" );
                first = false;
            } else {
                ret.append( "&" );
            }

            ret.append( param.getKey() + "=" + URLEncoder.encode( param.getValue() ) );
        }

        return ret.toString();
    }

    public static InputStream download( final long id ) {
        final Map< String , String > params = Maps.newHashMap();
        params.put( "documentId" , String.valueOf( id ) );

        return httpGet( ConstUtil.ENDPOINT + "download" , params );
    }

    private static InputStream getDecompressedStream( final HttpResponse res ) throws IOException {
        final Header encodingHeader = res.getEntity().getContentEncoding();
        if ( encodingHeader != null ) {
            final String encoding = encodingHeader.getValue().toLowerCase();

            if ( encoding.equals( "gzip" ) ) {
                return new GzipDecompressingEntity( res.getEntity() ).getContent();
            } else if ( encoding.equals( "deflate" ) ) {
                return new DeflateDecompressingEntity( res.getEntity() ).getContent();
            }
        }

        return res.getEntity().getContent();
    }

    public static InputStream getPage( final long id , final int page , final int level , final int px , final int py ) {
        final Map< String , String > params = Maps.newHashMap();
        params.put( "type" , "iPad" );
        params.put( "documentId" , String.valueOf( id ) );
        params.put( "page" , String.valueOf( page ) );
        params.put( "level" , String.valueOf( level ) );
        params.put( "px" , String.valueOf( px ) );
        params.put( "py" , String.valueOf( py ) );

        return httpGet( ConstUtil.ENDPOINT + "getPage" , params );
    }

    private static InputStream httpGet( final String url ) {
        try {
            final HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout( params , TIMEOUT );

            final HttpResponse res = new DefaultHttpClient( params ).execute( buildGet( url ) );

            final int statusCode = res.getStatusLine().getStatusCode();
            if ( statusCode < 400 ) {
                return getDecompressedStream( res );
            } else {
                throw new RuntimeException();
            }
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private static InputStream httpGet( final String url , final Map< String , String > params ) {
        return httpGet( url + buildParam( params ) );
    }
}
