package jp.archilogic.docnext.logic;

import java.awt.Rectangle;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.archilogic.docnext.bean.PropBean;
import jp.archilogic.docnext.util.ProcUtil;

import magick.MagickException;
import magick.MagickImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailCreator {
    private class ImageInfo {
        double unitWidth;
        double unitHeight;

        ImageInfo( double unitWidth , double unitHeight ) {
            this.unitWidth = unitWidth;
            this.unitHeight = unitHeight;
        }
    }

    static {
    	System.setProperty( "jmagick.systemclassloader" , "no" );
    }

    private static final Logger LOGGER = LoggerFactory.getLogger( ThumbnailCreator.class );

    private static final int IPAD_MAX_LEVEL = 2;
    private static final int IPAD_DEVICE_WIDTH = 768;
    private static final int IPAD_DEVICE_HEIGHT = 1024 - 20;
    private static final int IPHONE_MAX_LEVEL = 3;
    private static final int IPHONE_DEVICE_WIDTH = 320;
    private static final int IPHONE_DEVICE_HEIGHT = 480 - 20;
    private static final int THUMBNAIL_SIZE = 256;

    @Autowired
    private PropBean prop;

    private ImageInfo calcBaseResolution( String pdfPath , String prefix ) {
        final int SAMPLE_RESOLUTION = 100;

        createByResolution( pdfPath , prefix , 0 , SAMPLE_RESOLUTION );

        int[] size = getImageSize( getPpmPath( prefix , 0 ) );
        int sampleWidth = size[ 0 ];
        int sampleHeight = size[ 1 ];

        return new ImageInfo( 1.0 * sampleWidth / SAMPLE_RESOLUTION , 1.0 * sampleHeight / SAMPLE_RESOLUTION );
    }

    private MagickImage convertAndResize( String ppmPath , String pngPath , int width , int height , double imageWidth ,
            double imageHeight ) throws MagickException {
        int borderWidth = ( int ) Math.round( ( width - imageWidth ) / 2.0 );
        int borderHeight = ( int ) Math.round( ( height - imageHeight ) / 2.0 );

        MagickImage mi = new MagickImage(new magick.ImageInfo(ppmPath));
        Rectangle border = new Rectangle( borderWidth, borderHeight );
        mi = mi.borderImage( border );
        
        return mi;
    }

    /**
     * @return width / height ratio
     */
    public double create( String outDir , String pdfPath , String prefix ) {
        LOGGER.info( "Begin create thumbanil" );
        long t = System.currentTimeMillis();

        new File( outDir ).mkdir();

        ImageInfo info = calcBaseResolution( pdfPath , prefix );

        try {
	        for ( int page = 0 , pages = getPages( pdfPath ) ; page < pages ; page++ ) {
	            LOGGER.info( "Proc page: " + page );
	
	            createImage( outDir + "iPad" , pdfPath , prefix , info , page , IPAD_MAX_LEVEL , IPAD_DEVICE_WIDTH ,
	                    IPAD_DEVICE_HEIGHT );
	            createImage( outDir + "iPhone" , pdfPath , prefix , info , page , IPHONE_MAX_LEVEL , IPHONE_DEVICE_WIDTH ,
	                    IPHONE_DEVICE_HEIGHT );
	            createThumbnail( outDir , pdfPath , prefix , info , page );
	        }
        } catch (MagickException e) {
        	throw new RuntimeException("Can't create image.", e);
		}

        LOGGER.info( "End create thumbnail. Tooks " + ( System.currentTimeMillis() - t ) + "(ms)" );

        return info.unitWidth / info.unitHeight;
    }

    private void createByResolution( String pdfPath , String prefix , int page , int resolution ) {
        ProcUtil.doProc( String.format( "%s -r %d -f %d -l %d %s %s" , prop.pdfToPpm , resolution , page + 1 ,
                page + 1 , pdfPath , prefix ) , true );
    }

    private void createImage( String outPath , String pdfPath , String prefix , ImageInfo info , int page ,
            int maxLevel , int deviceWidth , int deviceHeight ) throws MagickException {
        int maxFactor = ( int ) Math.pow( 2 , maxLevel - 1 );

        double maxResolution;
        if ( info.unitWidth * deviceHeight > info.unitHeight * deviceWidth ) {
            maxResolution = deviceWidth / info.unitWidth * maxFactor;
        } else {
            maxResolution = deviceHeight / info.unitHeight * maxFactor;
        }

        createByResolution( pdfPath , prefix , page , ( int ) Math.ceil( maxResolution ) );
        MagickImage mi = convertAndResize( getPpmPath( prefix , page ) , getPngPath( prefix , page ) ,
        		deviceWidth * maxFactor , deviceHeight * maxFactor ,
        		info.unitWidth * maxResolution , info.unitHeight * maxResolution );

        for ( int level = 0 ; level < maxLevel ; level++ ) {
            LOGGER.info( "Proc level: " + level );

            int factor = ( int ) Math.pow( 2 , level );

            for ( int py = 0 ; py < factor ; py++ ) {
                for ( int px = 0 ; px < factor ; px++ ) {
                    LOGGER.info( "Proc part: " + px + "," + py );

                    int w = deviceWidth * maxFactor / factor;
                    int h = deviceHeight * maxFactor / factor;
                    cropAndResize( mi ,
                            String.format( "%s%d-%d-%d-%d.jpg" , outPath , page , level , px , py ) , px * w , py * h ,
                            w , h , deviceWidth , deviceHeight );
                }
            }
        }
    }

    private void createThumbnail( String outDir , String pdfPath , String prefix , ImageInfo info , int page ) throws MagickException {
        double resolution = THUMBNAIL_SIZE / Math.max( info.unitWidth , info.unitHeight );

        int w = ( int ) Math.round( info.unitWidth * resolution );
        int h = ( int ) Math.round( info.unitHeight * resolution );

        createByResolution( pdfPath , prefix , page , ( int ) Math.ceil( resolution ) );
        MagickImage mi = convertAndResize( getPpmPath( prefix , page ) , getPngPath( prefix , page ) , w , h , w , h );
        cropAndResize( mi , String.format( "%sthumb-%d.jpg" , outDir , page ) , 0 , 0 , w , h ,
                w , h );
    }

    private void cropAndResize( MagickImage image , String destPath , int x , int y , int cropWidth , int cropHeight ,
            int resizeWidth , int resizeHeight ) throws MagickException {
    	Rectangle crop = new Rectangle( x , y , cropWidth , cropHeight );
        MagickImage croped = image.cropImage( crop );
        MagickImage scale = croped.scaleImage( resizeWidth, resizeHeight );
        scale.setFileName( destPath );
        scale.writeImage( new magick.ImageInfo() );
        
        LOGGER.debug( "Convert image:" + destPath);
    }

    private int[] getImageSize( String path ) {
        String[] sizes =
                ProcUtil.doProc( String.format( "%s -ping %s" , prop.identify , path ) ).split( " " )[ 2 ].split( "x" );
        return new int[] { Integer.parseInt( sizes[ 0 ] ) , Integer.parseInt( sizes[ 1 ] ) };
    }

    public int getPages( String pdfPath ) {
        Matcher matcher =
                Pattern.compile( "Pages: +([0-9]+)" ).matcher(
                        ProcUtil.doProc( String.format( "%s %s" , prop.pdfInfo , pdfPath ) , true ) );

        if ( !matcher.find() ) {
            throw new RuntimeException( "No pages found" );
        }

        return Integer.parseInt( matcher.group( 1 ) );
    }

    private String getPngPath( String prefix , int page ) {
        return String.format( "%s-%06d.png" , prefix , page + 1 );
    }

    private String getPpmPath( String prefix , int page ) {
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
