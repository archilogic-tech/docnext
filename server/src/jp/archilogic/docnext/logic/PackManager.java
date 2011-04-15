package jp.archilogic.docnext.logic;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import jp.archilogic.docnext.bean.PropBean;
import jp.archilogic.docnext.dto.DividePage;
import jp.archilogic.docnext.dto.Frame;
import jp.archilogic.docnext.dto.Region;
import jp.archilogic.docnext.dto.TOCElem;
import jp.archilogic.docnext.logic.PDFAnnotationParser.PageAnnotationInfo;
import net.arnx.jsonic.JSON;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class PackManager {
    @SuppressWarnings( "unused" )
    private static final Logger LOGGER = LoggerFactory.getLogger( PackManager.class );

    @Autowired
    private PropBean prop;

    private void copyImageHelper( final long documentId , final String fileFormat ) {
        final String dir = prop.repository + "/pack/" + documentId + "/images/";

        try {
            for ( int index = 0 ; ; index++ ) {
                final File file =
                        new File( String.format( fileFormat , prop.repository , documentId , index ) );
                if ( !file.exists() ) {
                    break;
                }
                FileUtils.copyFileToDirectory( file , new File( dir ) );
            }
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void copyThumbnails( final long documentId ) {
        copyImageHelper( documentId , "%s/thumb/%d/thumb-%d.jpg" );
    }

    public void createStruct( final long documentId ) {
        final String dir = prop.repository + "/pack/" + documentId + "/";

        new File( dir ).mkdir();
        new File( dir + "texts/" ).mkdir();
    }

    public String getPackPath( final long documentId ) {
        return String.format( "%s/pack/%d.zip" , prop.repository , documentId );
    }

    public String readAnnotation( final long documentId , final int page ) {
        try {
            return FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/images/%d.anno.json" , prop.repository , documentId , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public List< DividePage > readDividePage( final long documentId ) {
        try {
            return Arrays.asList( JSON.decode( FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/dividePage.json" , prop.repository , documentId ) ) ) ,
                    DividePage[].class ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public String readFrameJson( final long docId ) {
        try {
            return FileUtils.readFileToString( new File( String.format( "%s/pack/%d/frames.json" ,
                    prop.repository , docId ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public List< Frame > readFrames( final long documentId ) {
        return Arrays.asList( JSON.decode( readFrameJson( documentId ) , Frame[].class ) );
    }

    public String readImageText( final long documentId , final int page ) {
        try {
            return FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/texts/%d.image.txt" , prop.repository , documentId , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public String readInfoJson( final long docId ) {
        try {
            return FileUtils.readFileToString(
                    new File( String.format( "%s/pack/%d/info.json" , prop.repository , docId ) ) ,
                    "UTF-8" );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public byte[] readRegions( final long docId , final int page ) {
        try {
            return FileUtils.readFileToByteArray( new File( String.format(
                    "%s/pack/%d/texts/%d.regions" , prop.repository , docId , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public List< Integer > readSinglePageInfo( final long documentId ) {
        try {
            return Arrays.asList( JSON.decode( FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/singlePageInfo.json" , prop.repository , documentId ) ) ) ,
                    Integer[].class ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public String readText( final long documentId , final int page ) {
        try {
            return FileUtils.readFileToString( new File( String.format( "%s/pack/%d/texts/%d" ,
                    prop.repository , documentId , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public List< TOCElem > readTOC( final long documentId ) {
        try {
            return Arrays.asList( JSON.decode( FileUtils.readFileToString( new File( String.format(
                    "%s/pack/%d/toc.json" , prop.repository , documentId ) ) ) , TOCElem[].class ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeAnnotations( final long documentId , final int page ,
            final List< PageAnnotationInfo > infos ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/images/%d.anno.json" , prop.repository ,
                            documentId , page ) ) , JSON.encode( infos ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeDividePage( final long documentId , final List< DividePage > dividePage ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/dividePage.json" , prop.repository ,
                            documentId ) ) , JSON.encode( dividePage ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeFrames( final long documentId , final List< Frame > frames ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/frames.json" , prop.repository ,
                            documentId ) ) , JSON.encode( frames ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeImageText( final long documentId , final int page , final String text ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/texts/%d.image.txt" , prop.repository ,
                            documentId , page ) ) , text );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeRegions( final long documentId , final int page , final List< Region > regions ) {
        final int SIZEOF_DOUBLE = 8;
        final int N_REGION_FIELDS = 4;

        try {
            final ByteBuffer buffer =
                    ByteBuffer.allocate( regions.size() * N_REGION_FIELDS * SIZEOF_DOUBLE );
            buffer.order( ByteOrder.LITTLE_ENDIAN );
            for ( final Region region : regions ) {
                buffer.putDouble( region.x );
                buffer.putDouble( region.y );
                buffer.putDouble( region.width );
                buffer.putDouble( region.height );
            }
            FileUtils.writeByteArrayToFile(
                    new File( String.format( "%s/pack/%d/texts/%d.regions" , prop.repository ,
                            documentId , page ) ) , buffer.array() );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeSinglePageInfo( final long documentId , final List< Integer > singlePageInfo ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/singlePageInfo.json" , prop.repository ,
                            documentId ) ) , JSON.encode( singlePageInfo ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeText( final long documentId , final int page , final String text ) {
        try {
            FileUtils.writeStringToFile(
                    new File( String.format( "%s/pack/%d/texts/%d" , prop.repository , documentId ,
                            page ) ) , text );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public void writeTOC( final long documentId , final List< TOCElem > toc ) {
        try {
            FileUtils
                    .writeStringToFile(
                            new File( String.format( "%s/pack/%d/toc.json" , prop.repository ,
                                    documentId ) ) , JSON.encode( toc ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
