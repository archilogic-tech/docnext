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
    private class ImageInfo {
        double unitWidth;
        double unitHeight;

        ImageInfo( final double unitWidth , final double unitHeight ) {
            this.unitWidth = unitWidth;
            this.unitHeight = unitHeight;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger( ThumbnailCreator.class );

    private static final int IPAD_MAX_LEVEL = 2;
    private static final int IPAD_DEVICE_WIDTH = 768;
    private static final int IPAD_DEVICE_HEIGHT = 1024 - 20;
    private static final int IPHONE_MAX_LEVEL = 3;
    private static final int IPHONE_DEVICE_WIDTH = 320;
    private static final int IPHONE_DEVICE_HEIGHT = 480 - 20;
    private static final int THUMBNAIL_SIZE = 256;
    private static final int WEB_HEIGHT = 1600;
    private static final int TEXTURE_BASE_SIZE = 256;
    private static final int TEXTURE_HORIZONATL = 4;
    private static final int TEXTURE_VERTICAL = 6;

    @Autowired
    private PropBean prop;
    @Autowired
    private ProgressManager progressManager;

    private ImageInfo calcBaseResolution( final String pdfPath , final String prefix ) {
        final int SAMPLE_RESOLUTION = 100;

        createByResolution( pdfPath , prefix , 0 , SAMPLE_RESOLUTION );

        final int[] size = getImageSize( getPpmPath( prefix , 0 ) );
        final int sampleWidth = size[ 0 ];
        final int sampleHeight = size[ 1 ];

        return new ImageInfo( 1.0 * sampleWidth / SAMPLE_RESOLUTION , 1.0 * sampleHeight / SAMPLE_RESOLUTION );
    }

    private void convertAndResize( final String ppmPath , final String pngPath , final int width , final int height ,
            final double imageWidth , final double imageHeight , final boolean forTexture ) {
        final int borderWidth = ( int ) Math.round( ( width - imageWidth ) / 2.0 );
        final int borderHeight = ( int ) Math.round( ( height - imageHeight ) / 2.0 );

        if ( forTexture ) {
            ProcUtil.doProc( String.format(
                    "%s %s -format png -resize %dx%d -gravity southeast -background #00ff00 -splice %dx%d %s" ,
                    prop.convert , ppmPath , width , height , borderWidth * 2 , borderHeight * 2 , pngPath ) );
        } else {
            ProcUtil.doProc( String.format( "%s %s -format png -resize %dx%d -border %dx%d %s" , prop.convert ,
                    ppmPath , width , height , borderWidth , borderHeight , pngPath ) );
        }
    }

    /**
     * @return width / height ratio
     */
    public double create( final String outDir , final String pdfPath , final String prefix , final long id ) {
        LOGGER.info( "Begin create thumbanil" );
        final long t = System.currentTimeMillis();

        new File( outDir ).mkdir();

        final ImageInfo info = calcBaseResolution( pdfPath , prefix );

        for ( int page = 0 , pages = getPages( pdfPath ) ; page < pages ; page++ ) {
            LOGGER.info( "Proc page: " + page );

            if ( prop.forIOS ) {
                createImage( outDir + "iPad" , pdfPath , prefix , info , page , IPAD_MAX_LEVEL , IPAD_DEVICE_WIDTH ,
                        IPAD_DEVICE_HEIGHT );
                createImage( outDir + "iPhone" , pdfPath , prefix , info , page , IPHONE_MAX_LEVEL ,
                        IPHONE_DEVICE_WIDTH , IPHONE_DEVICE_HEIGHT );
            }

            if ( prop.forWeb ) {
                createWeb( outDir , pdfPath , prefix , info , page );
            }

            if ( prop.forTexture ) {
                createTextureImage( outDir + "texture256-" , pdfPath , prefix , info , page , IPAD_MAX_LEVEL , 1 );
                createTextureImage( outDir + "texture512-" , pdfPath , prefix , info , page , IPAD_MAX_LEVEL , 2 );
            }

            createThumbnail( outDir , pdfPath , prefix , info , page );

            progressManager.setCreatedThumbnail( id , page + 1 );
        }

        LOGGER.info( "End create thumbnail. Tooks " + ( System.currentTimeMillis() - t ) + "(ms)" );

        return info.unitWidth / info.unitHeight;
    }

    private void createByResolution( final String pdfPath , final String prefix , final int page , final int resolution ) {
        ProcUtil.doProc( String.format( "%s -r %d -f %d -l %d %s %s" , prop.pdfToPpm , resolution , page + 1 ,
                page + 1 , pdfPath , prefix ) , true );
    }

    private void createImage( final String outPath , final String pdfPath , final String prefix , final ImageInfo info ,
            final int page , final int maxLevel , final int deviceWidth , final int deviceHeight ) {
        final int maxFactor = ( int ) Math.pow( 2 , maxLevel - 1 );

        double maxResolution;
        if ( info.unitWidth * deviceHeight > info.unitHeight * deviceWidth ) {
            maxResolution = deviceWidth / info.unitWidth * maxFactor;
        } else {
            maxResolution = deviceHeight / info.unitHeight * maxFactor;
        }

        createByResolution( pdfPath , prefix , page , ( int ) Math.ceil( maxResolution ) );
        convertAndResize( getPpmPath( prefix , page ) , getPngPath( prefix , page ) , deviceWidth * maxFactor ,
                deviceHeight * maxFactor , info.unitWidth * maxResolution , info.unitHeight * maxResolution , false );

        for ( int level = 0 ; level < maxLevel ; level++ ) {
            LOGGER.info( "Proc level: " + level );

            final int factor = ( int ) Math.pow( 2 , level );

            for ( int py = 0 ; py < factor ; py++ ) {
                for ( int px = 0 ; px < factor ; px++ ) {
                    LOGGER.info( "Proc part: " + px + "," + py );

                    final int w = deviceWidth * maxFactor / factor;
                    final int h = deviceHeight * maxFactor / factor;
                    cropAndResize( getPngPath( prefix , page ) ,
                            String.format( "%s%d-%d-%d-%d.jpg" , outPath , page , level , px , py ) , px * w , py * h ,
                            w , h , deviceWidth , deviceHeight );
                }
            }
        }
    }

    private void createTextureImage( final String outPath , final String pdfPath , final String prefix ,
            final ImageInfo info , final int page , final int maxLevel , final int textureFactor ) {
        final int deviceWidth = TEXTURE_BASE_SIZE * TEXTURE_HORIZONATL;
        final int deviceHeight = TEXTURE_BASE_SIZE * TEXTURE_VERTICAL;
        final int maxFactor = ( int ) Math.pow( 2 , maxLevel - 1 );

        double maxResolution;
        if ( info.unitWidth * deviceHeight > info.unitHeight * deviceWidth ) {
            maxResolution = deviceWidth / info.unitWidth * maxFactor;
        } else {
            maxResolution = deviceHeight / info.unitHeight * maxFactor;
        }

        createByResolution( pdfPath , prefix , page , ( int ) Math.ceil( maxResolution ) );
        convertAndResize( getPpmPath( prefix , page ) , getPngPath( prefix , page ) , deviceWidth * maxFactor ,
                deviceHeight * maxFactor , info.unitWidth * maxResolution , info.unitHeight * maxResolution , true );

        for ( int level = 0 ; level < maxLevel ; level++ ) {
            LOGGER.info( "Proc level: " + level );

            final int factor = ( int ) Math.pow( 2 , level );

            for ( int py = 0 ; py < TEXTURE_VERTICAL / textureFactor * factor ; py++ ) {
                for ( int px = 0 ; px < TEXTURE_HORIZONATL / textureFactor * factor ; px++ ) {
                    LOGGER.info( "Proc part: " + px + "," + py );

                    final int w = TEXTURE_BASE_SIZE * textureFactor * maxFactor / factor;
                    final int h = TEXTURE_BASE_SIZE * textureFactor * maxFactor / factor;
                    cropAndResize( getPngPath( prefix , page ) ,
                            String.format( "%s%d-%d-%d-%d.jpg" , outPath , page , level , px , py ) , px * w , py * h ,
                            w , h , TEXTURE_BASE_SIZE * textureFactor , TEXTURE_BASE_SIZE * textureFactor );
                }
            }
        }
    }

    private void createThumbnail( final String outDir , final String pdfPath , final String prefix ,
            final ImageInfo info , final int page ) {
        final double resolution = THUMBNAIL_SIZE / Math.max( info.unitWidth , info.unitHeight );

        final int w = ( int ) Math.round( info.unitWidth * resolution );
        final int h = ( int ) Math.round( info.unitHeight * resolution );

        createByResolution( pdfPath , prefix , page , ( int ) Math.ceil( resolution ) );
        convertAndResize( getPpmPath( prefix , page ) , String.format( "%sthumb-%d.jpg" , outDir , page ) , w , h , w ,
                h , false );
    }

    private void createWeb( final String outDir , final String pdfPath , final String prefix , final ImageInfo info ,
            final int page ) {
        final double resolution = WEB_HEIGHT / info.unitHeight;

        final int w = ( int ) Math.round( info.unitWidth * resolution );
        final int h = ( int ) Math.round( info.unitHeight * resolution );

        createByResolution( pdfPath , prefix , page , ( int ) Math.ceil( resolution ) );
        convertAndResize( getPpmPath( prefix , page ) , String.format( "%sweb-%d.jpg" , outDir , page ) , w , h , w ,
                h , false );
    }

    private void cropAndResize( final String pngPath , final String destPath , final int x , final int y ,
            final int cropWidth , final int cropHeight , final int resizeWidth , final int resizeHeight ) {
        ProcUtil.doProc( String.format( "%s %s -crop %dx%d+%d+%d -resize %dx%d %s" , prop.convert , pngPath ,
                cropWidth , cropHeight , x , y , resizeWidth , resizeHeight , destPath ) );
    }

    private int[] getImageSize( final String path ) {
        final String[] sizes =
                ProcUtil.doProc( String.format( "%s -ping %s" , prop.identify , path ) ).split( " " )[ 2 ].split( "x" );
        return new int[] { Integer.parseInt( sizes[ 0 ] ) , Integer.parseInt( sizes[ 1 ] ) };
    }

    public int getPages( final String pdfPath ) {
        final Matcher matcher =
                Pattern.compile( "Pages: +([0-9]+)" ).matcher(
                        ProcUtil.doProc( String.format( "%s %s" , prop.pdfInfo , pdfPath ) , true ) );

        if ( !matcher.find() ) {
            throw new RuntimeException( "No pages found" );
        }

        return Integer.parseInt( matcher.group( 1 ) );
    }

    private String getPngPath( final String prefix , final int page ) {
        return String.format( "%s-%06d.png" , prefix , page + 1 );
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
}
