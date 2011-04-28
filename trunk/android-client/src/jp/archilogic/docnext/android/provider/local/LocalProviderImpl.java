package jp.archilogic.docnext.android.provider.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.info.BookmarkInfo;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.info.TOCElement;
import jp.archilogic.docnext.android.info.TextInfo;
import jp.archilogic.docnext.android.task.DownloadTask;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;

public class LocalProviderImpl implements LocalProvider {
    private final LocalPathManager _pathManager = new LocalPathManager();
    private final int TEXTURE_SIZE = 512;

    @Override
    public List< BookmarkInfo > getBookmarkInfo( final long id ) {
        BookmarkInfo[] bookmarks = null;
        try {
            bookmarks = getJsonInfo( _pathManager.getBookmarkPath( id ) , BookmarkInfo[].class );
        } catch ( JSONException exception ) {
            // assume json format is old
            Integer[] array = getJsonInfo( _pathManager.getBookmarkPath( id ) , Integer[].class );
            bookmarks = new BookmarkInfo[ array.length ];
            for ( int i = 0 ; i < bookmarks.length ; i++ ) {
                bookmarks[ i ] = new BookmarkInfo( array[ i ] );
            }
        }
        if ( bookmarks == null ) {
            try {
                FileUtils.writeStringToFile( new File( _pathManager.getBookmarkPath( id ) ) , "[]" );
            } catch ( IOException e ) {
            }
            return null;
        }

        for ( BookmarkInfo bookmark : bookmarks ) {
            bookmark.text = getTOCText( id , bookmark.page );
        }
        return new LinkedList< BookmarkInfo >( Arrays.asList( bookmarks ) );
    }

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
        ImageInfo imageInfo = getJsonInfo( _pathManager.getImageInfoPath( id ) , ImageInfo.class );
        if ( imageInfo != null )
            imageInfo.nLevel = 1;
        return imageInfo;
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
    public int getLastOpenedPage( final long id ) {
        try {
            return Integer.valueOf( FileUtils.readFileToString( new File( _pathManager.getLastOpenedPagePath( id ) ) ).trim() );
        } catch ( final Exception e ) {
            return -1;
        }
    }
    
    @Override
    public List< Integer > getSinglePages( final long id ) {
        Integer[] array = getJsonInfo( _pathManager.getSinglePagesPath( id ) , Integer[].class ); 
        return array == null ? new ArrayList< Integer >() : Arrays.asList( array );
    }

    @Override
    public List< Integer > getSpreadFirstPages( long id ) {
        DocInfo doc = getDocInfo( id );

        List< Integer > singlePages = getSinglePages( id );
        List< Integer > spreadFirstPages = new ArrayList< Integer >();
        
        for ( int page = 0 ; page < doc.pages ; page++ ) {
            if ( !singlePages.contains( page ) && 
                    ( page == 0 || !spreadFirstPages.contains( page - 1 ) ) ) {
                spreadFirstPages.add( page );
            }
        }
        return spreadFirstPages;
    }

    @Override
    public List<TOCElement> getTableOfContentsInfo( final long id ) {
        TOCElement[] tocs;
        try {
            tocs =  getJsonInfo( _pathManager.getTableOfContentsInfoPath( id ) , TOCElement[].class );
        } catch ( Exception  e ) {
            tocs = new TOCElement[]{};
        }
        return tocs == null ? null : Arrays.asList( tocs );
    }

    @Override
    public TextInfo getText( final long id , final int page ) {
        return getJsonInfo( _pathManager.getTextPath( id , page ) , TextInfo.class );
    }

    @Override
    public String getThumbnailPath( final long id , final int page ) {
        String path = _pathManager.getThumbnailPath( id , page );
        
        if ( ( new File( path ) ).exists() ) {
            return path;
        }
        return null;
    }

    @Override
    public String getTOCText( final long id , final int page ) {
        List< TOCElement > toc = getTableOfContentsInfo( id );
        TreeSet< TOCElement > set = new TreeSet< TOCElement >();
        if ( toc != null ) {
            set.addAll( toc );
        }
        TOCElement[] array = new TOCElement[ set.size() ];
        set.toArray( array );

        String text = "NO TITLE";
        for ( int i = 0 ; i < array.length ; i++ ) {
            if ( i == array.length - 1 ) {
                text = array[ i ].text;
                break;
            }
            if ( array[ i ].page <= page && array[ i + 1 ].page > page ) {
                text = array[ i ].text;
                break;
            }
        }
        return text;
    }
    
    public boolean isAllImageExists( final long id , final int page ) {
        final ImageInfo image = Kernel.getLocalProvider().getImageInfo( id );

        final int nx = image.width / TEXTURE_SIZE;
        final int ny = image.height / TEXTURE_SIZE;

        for ( int py = 0 ; py < ny ; py++ ) {
            for ( int px = 0 ; px < nx ; px++ ) {
                String path = Kernel.getLocalProvider().getImagePath( id , page , 0 , px , py );
                if ( path == null ) {
                    return false;
                }
            }
        }
        return true;
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
    
    public boolean isImageExists( final long id , final int page , final int level , final int px , final int py ) {
        String path = getImagePath( id , page , level , px , py );

        return path != null && 
            !( new File( path + DownloadTask.DOWNLOADING_POSTFIX ) ).exists();
    }

    @Override
    public void setBookmarkInfo( final long id , final List< BookmarkInfo > bookmarks ) {
        if ( bookmarks == null ) {
            setJsonInfo( _pathManager.getBookmarkPath( id ) , new ArrayList< BookmarkInfo >( 0 ) );
        } else {
            setJsonInfo( _pathManager.getBookmarkPath( id ) , bookmarks );
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

    @Override
    public void setLastOpenedPage( final long id , final int page ) {
        try {
            _pathManager.ensureRoot();

            FileUtils.writeStringToFile( new File( _pathManager.getLastOpenedPagePath( id ) ) , Integer.toString( page ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
