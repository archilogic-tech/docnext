package jp.archilogic.docnext.android.provider.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.info.TextInfo;
import net.arnx.jsonic.JSON;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;

public class LocalProviderImpl implements LocalProvider {
    private final LocalPathManager _pathManager = new LocalPathManager();

    @Override
    public DocInfo getDocInfo( final long id ) {
        return getJsonInfo( _pathManager.getDocInfoPath( id ) , DocInfo.class );
    }

    @Override
    public String getFontPath( final String name ) {
        final String ret = _pathManager.getFontPath( name );

        if ( !new File( ret ).exists() ) {
            return null;
        }

        return ret;
    }

    @Override
    public ImageInfo getImageInfo( final long id ) {
        return getJsonInfo( _pathManager.getImageInfoPath( id ) , ImageInfo.class );
    }

    @Override
    public String getImagePath( final long id , final int page , final int level , final int px , final int py ) {
        final String ret = _pathManager.getImagePath( id , page , level , px , py );

        if ( !new File( ret ).exists() ) {
            return null;
        }

        return ret;
    }

    private < T > T getJsonInfo( final String path , final Class< ? extends T > cls ) {
        if ( !new File( path ).exists() ) {
            return null;
        }

        InputStream in = null;
        try {
            in = new FileInputStream( path );

            return JSON.decode( in , cls );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    @Override
    public TextInfo getTextInfo( final long id , final int page ) {
        return getJsonInfo( _pathManager.getTextInfoPath( id , page ) , TextInfo.class );
    }

    @Override
    public boolean isCompleted( final long id ) {
        // Use database?
        final Long[] completed = getJsonInfo( _pathManager.getCompletedInfoPath() , Long[].class );

        if ( completed == null ) {
            return false;
        }

        return Arrays.asList( completed ).contains( id );
    }

    @Override
    public boolean isImageExists( final long id , final int page ) {
        if ( page + 1 < getDocInfo( id ).pages ) {
            return getImagePath( id , page + 1 , 0 , 0 , 0 ) != null;
        } else {
            return isCompleted( id );
        }
    }

    @Override
    public void setCompleted( final long id ) {
        final Long[] completed = getJsonInfo( _pathManager.getCompletedInfoPath() , Long[].class );

        final List< Long > list = completed != null ? Lists.newArrayList( completed ) : Lists.< Long > newArrayList();
        list.add( id );

        setJsonInfo( _pathManager.getCompletedInfoPath() , list );
    }

    private void setJsonInfo( final String path , final Object source ) {
        OutputStream out = null;
        try {
            out = new FileOutputStream( path );

            JSON.encode( source , out );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( out );
        }
    }
}
