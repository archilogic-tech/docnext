package jp.archilogic.android.downloader.internal;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
    static class PartialStreamWithLength extends StreamWithLength {
        private final boolean _isPartial;

        protected PartialStreamWithLength( final InputStream in , final long contentLength , final boolean isPartial ) {
            super( in , contentLength );

            _isPartial = isPartial;
        }

        public boolean isPartial() {
            return _isPartial;
        }
    }

    static class StreamWithLength {
        private final InputStream _in;
        private final long _contentLength;

        protected StreamWithLength( final InputStream in , final long contentLength ) {
            _in = in;
            _contentLength = contentLength;
        }

        public long getContentLength() {
            return _contentLength;
        }

        public InputStream getInputStream() {
            return _in;
        }
    }

    private static final int TIMEOUT = 10 * 1000;

    private static PartialStreamWithLength asPartialStreamWithLength( final HttpResponse res ) throws IOException {
        final int statusCode = res.getStatusLine().getStatusCode();

        if ( statusCode < 400 ) {
            return new PartialStreamWithLength( decompress( res.getEntity() ) , res.getEntity().getContentLength() ,
                    statusCode == HttpStatus.SC_PARTIAL_CONTENT );
        } else {
            throw new IOException( "Invalid statusCode: " + statusCode );
        }
    }

    private static HttpResponse asResponse( final HttpUriRequest req ) throws IOException {
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout( params , TIMEOUT );
        HttpConnectionParams.setSoTimeout( params , TIMEOUT );
        HttpClientParams.setRedirecting( params , true );

        return new DefaultHttpClient( params ).execute( req );
    }

    private static StreamWithLength asStreamWithLength( final HttpResponse res ) throws IOException {
        final int statusCode = res.getStatusLine().getStatusCode();

        if ( statusCode < 400 ) {
            return new StreamWithLength( decompress( res.getEntity() ) , res.getEntity().getContentLength() );
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

    public static StreamWithLength get( final String url ) throws IOException {
        final HttpGet get = new HttpGet( url );

        return asStreamWithLength( asResponse( get ) );
    }

    public static PartialStreamWithLength get( final String url , final long rangeFrom ) throws IOException {
        final HttpGet get = new HttpGet( url );
        get.addHeader( "Range" , String.format( "bytes=%d-" , rangeFrom ) );

        return asPartialStreamWithLength( asResponse( get ) );
    }
}
