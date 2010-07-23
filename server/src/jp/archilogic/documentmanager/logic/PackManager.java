package jp.archilogic.documentmanager.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jp.archilogic.documentmanager.bean.PropBean;
import jp.archilogic.documentmanager.dto.PageTextInfo;
import jp.archilogic.documentmanager.dto.TOCElem;
import net.arnx.jsonic.JSON;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class PackManager {
    class Info {
        public int pages;
        public String title;
        public String publisher;
        public double ratio;
    }

    @SuppressWarnings( "unused" )
    private static final Logger LOGGER = LoggerFactory.getLogger( PackManager.class );

    @Autowired
    private PropBean prop;

    private void copyImageHelper( long documentId , String fileFormat ) {
        String dir = prop.repository + "/pack/" + documentId + "/images/";

        try {
            for ( int index = 0 ; ; index++ ) {
                File file = new File( String.format( fileFormat , prop.repository , documentId , index ) );
                if ( !file.exists() ) {
                    break;
                }
                FileUtils.copyFileToDirectory( file , new File( dir ) );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void copyThumbnails( long documentId ) {
        copyImageHelper( documentId , "%s/thumb/%d/thumb-%d.jpg" );
    }

    public void createStruct( long documentId ) {
        String dir = prop.repository + "/pack/" + documentId + "/";

        new File( dir ).mkdir();
        new File( dir + "images/" ).mkdir();
        new File( dir + "texts/" ).mkdir();

        Info info = new Info();
        info.title = "NO TITLE";
        info.publisher = "NO PUBLISHER";
        writeInfo( documentId , info );
    }

    private void encode( ZipOutputStream zos , File[] files , int prefixLen ) throws Exception {
        for ( File f : files ) {
            if ( f.isDirectory() ) {
                encode( zos , f.listFiles() , prefixLen );
            } else {
                zos.putNextEntry( new ZipEntry( f.getPath().substring( prefixLen ).replace( '\\' , '/' ) ) );

                InputStream is = new BufferedInputStream( new FileInputStream( f ) );
                byte[] buf = new byte[ 1024 ];
                for ( ; ; ) {
                    int len = is.read( buf );
                    if ( len < 0 ) {
                        break;
                    }
                    zos.write( buf , 0 , len );
                }
                is.close();
                zos.closeEntry();
            }
        }
    }

    public String getPackPath( long documentId ) {
        return String.format( "%s/pack/%d.zip" , prop.repository , documentId );
    }

    private Info readInfo( long documentId ) {
        try {
            return JSON.decode( FileUtils.readFileToString( new File( String.format( "%s/pack/%d/info.json" ,
                    prop.repository , documentId ) ) ) , Info.class );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public String readPublisher( long documentId ) {
        return readInfo( documentId ).publisher;
    }

    @SuppressWarnings( "unchecked" )
    public List< Integer > readSinglePageInfo( long documentId ) {
        try {
            return Arrays.asList( JSON.decode( FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/singlePageInfo.json" , prop.repository , documentId ) ) ) , Integer[].class ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public String readText( long documentId , int page ) {
        try {
            return FileUtils.readFileToString( new File( String.format( "%s/pack/%d/texts/%d" , prop.repository ,
                    documentId , page ) ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public String readTitle( long documentId ) {
        return readInfo( documentId ).title;
    }

    @SuppressWarnings( "unchecked" )
    public List< TOCElem > readTOC( long documentId ) {
        try {
            return Arrays.asList( JSON.decode( FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/toc.json" , prop.repository , documentId ) ) ) , TOCElem[].class ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void repack( long documentId ) {
        try {
            String packed = prop.repository + "/pack/" + documentId + ".zip";
            String dir = prop.repository + "/pack/" + documentId + "/";

            ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( packed ) );
            encode( zos , new File[] { new File( dir ) } , dir.length() );
            zos.close();
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private void writeInfo( long documentId , Info info ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/info.json" , prop.repository , documentId ) ) ,
                    JSON.encode( info ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writePages( long documentId , int pages ) {
        Info info = readInfo( documentId );
        info.pages = pages;
        writeInfo( documentId , info );
    }

    public void writePublisher( long documentId , String publisher ) {
        Info info = readInfo( documentId );
        info.publisher = publisher;
        writeInfo( documentId , info );
    }

    public void writeRatio( long documentId , double ratio ) {
        Info info = readInfo( documentId );
        info.ratio = ratio;
        writeInfo( documentId , info );
    }

    public void writeSinglePageInfo( long documentId , List< Integer > singlePageInfo ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/singlePageInfo.json" , prop.repository , documentId ) ) ,
                    JSON.encode( singlePageInfo ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeText( long documentId , int page , String text ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/texts/%d" , prop.repository , documentId , page ) ) , text );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeTextInfo( long documentId , int page , PageTextInfo info ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/texts/%d.info" , prop.repository , documentId , page ) ) ,
                    JSON.encode( info ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeTitle( long documentId , String title ) {
        Info info = readInfo( documentId );
        info.title = title;
        writeInfo( documentId , info );
    }

    public void writeTOC( long documentId , List< TOCElem > toc ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/toc.json" , prop.repository , documentId ) ) ,
                    JSON.encode( toc ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
