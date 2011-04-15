package jp.archilogic.docnext.logic;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.archilogic.docnext.bean.PropBean;
import jp.archilogic.docnext.util.ProcUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailCreator {
    static class PDFInfo {
        int pages;
        int width;
        int height;
    }

    public static final int TEXTURE_SIZE = 512;

    private static final Logger LOGGER = LoggerFactory.getLogger( ThumbnailCreator.class );

    private static final int THUMBNAIL_SIZE = 256;
    private static final int WEB_HEIGHT = 1600;
    private static final int PDF_RESOLUTION = 300;

    @Autowired
    private PropBean prop;
    @Autowired
    private ProgressManager progressManager;

    private void createByResolution( final String pdfPath , final String prefix , final int page ,
            final int resolution ) {
        ProcUtil.doProc( String.format( "%s -r %d -f %d -l %d %s %s" , prop.pdfToPpm , resolution ,
                page + 1 , page + 1 , pdfPath , prefix ) , true );
    }

    public void createFromImage( final String outDir , final String imagePath , final int page ,
            final long id ) {
        LOGGER.info( "Begin create thumbanil" );
        final long t = System.currentTimeMillis();

        new File( outDir ).mkdir();

        LOGGER.info( "Proc page: " + page );

        if ( prop.forWeb ) {
            createWeb( outDir , imagePath , page );
        }

        if ( prop.forTexture ) {
            createTexture( outDir + "texture" , imagePath , page );
        }

        createThumbnail( outDir , imagePath , page );

        progressManager.setCreatedThumbnail( id , page + 1 );

        LOGGER.info( "End create thumbnail. Tooks " + ( System.currentTimeMillis() - t ) + "(ms)" );
    }

    public PDFInfo createFromPDF( final String outDir , final String pdfPath , final long id ) {
        final PDFInfo pdf = new PDFInfo();

        pdf.pages = getPages( pdfPath );

        final String prefix = prop.tmp + String.valueOf( id );

        for ( int page = 0 ; page < pdf.pages ; page++ ) {
            createByResolution( pdfPath , prefix , page , PDF_RESOLUTION );

            if ( page == 0 ) {
                final int[] size = getImageSize( getPpmPath( prefix , page ) );
                pdf.width = size[ 0 ];
                pdf.height = size[ 1 ];
            }

            createFromImage( outDir , getPpmPath( prefix , page ) , page , id );
        }

        return pdf;
    }

    private void createTexture( final String outPath , final String imagePath , final int page ) {
        final int[] size = getImageSize( imagePath );

        for ( int level = 0 ; ; level++ ) {
            LOGGER.info( "Proc level: " + level );

            final int factor = ( int ) Math.pow( 2 , level );

            if ( factor * TEXTURE_SIZE > size[ 0 ] ) {
                break;
            }

            final int l = size[ 0 ] / factor;

            for ( int py = 0 ; py * l < size[ 1 ] ; py++ ) {
                for ( int px = 0 ; px * l < size[ 0 ] ; px++ ) {
                    LOGGER.info( "Proc part: " + px + "," + py );

                    cropAndResize(
                            imagePath ,
                            String.format( "%s%d-%d-%d-%d.jpg" , outPath , page , level , px , py ) ,
                            px * l ,
                            py * l ,
                            l ,
                            l ,
                            TEXTURE_SIZE ,
                            TEXTURE_SIZE ,
                            Math.max(
                                    ( int ) Math.round( 1.0 * ( ( px + 1 ) * l - size[ 0 ] )
                                            * TEXTURE_SIZE / l ) , 0 ) ,
                            Math.max(
                                    ( int ) Math.round( 1.0 * ( ( py + 1 ) * l - size[ 1 ] )
                                            * TEXTURE_SIZE / l ) , 0 ) );
                }
            }
        }
    }

    private void createThumbnail( final String outDir , final String imagePath , final int page ) {
        resize( imagePath , String.format( "%sthumbnail%d.jpg" , outDir , page ) , THUMBNAIL_SIZE ,
                THUMBNAIL_SIZE );
    }

    private void createWeb( final String outDir , final String imagePath , final int page ) {
        resize( imagePath , String.format( "%sweb-%d.jpg" , outDir , page ) , Integer.MAX_VALUE ,
                WEB_HEIGHT );
    }

    private void cropAndResize( final String pngPath , final String destPath , final int x ,
            final int y , final int cropWidth , final int cropHeight , final int resizeWidth ,
            final int resizeHeight , final int padWidth , final int padHeight ) {
        if ( padWidth == 0 && padHeight == 0 ) {
            ProcUtil.doProc( String.format( "%s %s -crop %dx%d+%d+%d -resize %dx%d -quality 95 %s" ,
                    prop.convert , pngPath , cropWidth , cropHeight , x , y , resizeWidth ,
                    resizeHeight , destPath ) );
        } else {
            ProcUtil.doProc( String
                    .format(
                            "%s %s -crop %dx%d+%d+%d -resize %dx%d -gravity southeast -splice %dx%d -quality 95 %s" ,
                            prop.convert , pngPath , cropWidth , cropHeight , x , y , resizeWidth ,
                            resizeHeight , padWidth , padHeight , destPath ) );
        }
    }

    private int[] getImageSize( final String path ) {
        final String[] sizes =
                ProcUtil.doProc( String.format( "%s -ping %s" , prop.identify , path ) )
                        .split( " " )[ 2 ].split( "x" );
        return new int[] { Integer.parseInt( sizes[ 0 ] ) , Integer.parseInt( sizes[ 1 ] ) };
    }

    public int getPages( final String pdfPath ) {
        final Matcher matcher =
                Pattern.compile( "Pages: +([0-9]+)" )
                        .matcher(
                                ProcUtil.doProc( String.format( "%s %s" , prop.pdfInfo , pdfPath ) ,
                                        true ) );

        if ( !matcher.find() ) {
            throw new RuntimeException( "No pages found" );
        }

        return Integer.parseInt( matcher.group( 1 ) );
    }

    private String getPpmPath( final String prefix , final int page ) {
        String path = String.format( "%s-%06d.ppm" , prefix , page + 1 );
        if ( new File( path ).exists() ) {
            return path;
        }

        path = String.format( "%s-%d.ppm" , prefix , page + 1 );
        if ( new File( path ).exists() ) {
            return path;
        }

        path = String.format( "%s-%02d.ppm" , prefix , page + 1 );
        if ( new File( path ).exists() ) {
            return path;
        }

        throw new RuntimeException( "Could not find ppm file" );
    }

    private void resize( final String src , final String dst , final int maxWidth ,
            final int maxHeight ) {
        ProcUtil.doProc( String.format( "%s %s -resize %dx%d %s" , prop.convert , src ,
                THUMBNAIL_SIZE , THUMBNAIL_SIZE , dst ) );
    }
}
