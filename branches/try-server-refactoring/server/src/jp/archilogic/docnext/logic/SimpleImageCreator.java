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
public class SimpleImageCreator implements ImageCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger( SimpleImageCreator.class );

    private static final int IPAD_MAX_LEVEL = 2;
    private static final int IPAD_DEVICE_WIDTH = 768;
    private static final int IPAD_DEVICE_HEIGHT = 1024 - 20;
    private static final int IPHONE_MAX_LEVEL = 3;
    private static final int IPHONE_DEVICE_WIDTH = 320;
    private static final int IPHONE_DEVICE_HEIGHT = 480 - 20;
    private static final int THUMBNAIL_SIZE = 256;

    @Autowired
    private PropBean prop;
    
    static {
        System.setProperty( "jmagick.systemclassloader" , "no" );
    }

    protected ImageInfo createAllPpms( String pdfPath, String prefix) {
        LOGGER.info( "Create ppms." );
        int pages = getPages( pdfPath );
        ProcUtil.doProc( String.format( "%s -r %d -f %d -l %d %s %s" , prop.pdfToPpm , prop.resolution ,
                1 , pages , pdfPath , prefix ) , true );

        int[] size = getImageSize( getPpmPath( prefix , 0 ) );
        int width = size[ 0 ];
        int height = size[ 1 ];

        return new ImageInfo( 1.0 * width / prop.resolution , 1.0 * height / prop.resolution, pages );
    }

    protected MagickImage addBorder( MagickImage image , int width , int height , double imageWidth ,
            double imageHeight ) throws MagickException {
        LOGGER.info( "Add border" );
        int borderWidth = ( int ) Math.round( ( width - imageWidth ) / 2.0 );
        int borderHeight = ( int ) Math.round( ( height - imageHeight ) / 2.0 );

        Rectangle border = new Rectangle( borderWidth, borderHeight );
        MagickImage mi = image.borderImage( border );
        
        return mi;
    }

    /**
     * @return width / height ratio
     */
    public ImageInfo create( String outDir , String pdfPath , String prefix ) {
        LOGGER.info( "Begin create thumbanil" );
        long t = System.currentTimeMillis();

        new File( outDir ).mkdir();

        ImageInfo info = createAllPpms( pdfPath , prefix );
        createAllPages(outDir, prefix, info);

        LOGGER.info( "End create thumbnail. Tooks " + ( System.currentTimeMillis() - t ) + "(ms)" );

        return info;
    }

    protected void createAllPages( String outDir, String prefix, ImageInfo info ) {
        try {
	        for ( int page = 0 , pages = info.getPages() ; page < pages ; page++ ) {
	            LOGGER.info( "Proc page: " + page );
	            MagickImage mi = new MagickImage(new magick.ImageInfo( getPpmPath(prefix, page)));
	            
	            createImage( outDir + "iPad" , mi , info , page , IPAD_MAX_LEVEL , IPAD_DEVICE_WIDTH ,
	                    IPAD_DEVICE_HEIGHT );
	            createImage( outDir + "iPhone" , mi , info , page , IPHONE_MAX_LEVEL , IPHONE_DEVICE_WIDTH ,
	                    IPHONE_DEVICE_HEIGHT );
	            createThumbnail( outDir , mi , info , page );
	        }
        } catch (MagickException e) {
        	throw new RuntimeException("Can't create image.", e);
		}
    }

    protected void createByResolution( String pdfPath , String prefix , int page , int resolution ) {
        ProcUtil.doProc( String.format( "%s -r %d -f %d -l %d %s %s" , prop.pdfToPpm , resolution , page + 1 ,
                page + 1 , pdfPath , prefix ) , true );
    }

    protected void createImage( String outPath , MagickImage image , ImageInfo info , int page ,
            int maxLevel , int deviceWidth , int deviceHeight ) throws MagickException {
        int maxFactor = ( int ) Math.pow( 2 , maxLevel - 1 );
        double maxResolution = getMaxResolution(info, deviceWidth, deviceHeight, maxFactor);

        MagickImage mi = addBorder( image , deviceWidth * maxFactor , deviceHeight * maxFactor ,
                info.getUnitWidth() * maxResolution , info.getUnitHeight() * maxResolution );

        for ( int level = 0 ; level < maxLevel ; level++ ) {
            LOGGER.info( "Proc level: " + level );

            int factor = ( int ) Math.pow( 2 , level );
            int w = deviceWidth * maxFactor / factor;
            int h = deviceHeight * maxFactor / factor;

            for ( int py = 0 ; py < factor ; py++ ) {
                for ( int px = 0 ; px < factor ; px++ ) {
                    LOGGER.info( "Proc part: " + px + "," + py );

                    cropAndResize( mi ,
                            String.format( "%s%d-%d-%d-%d.jpg" , outPath , page , level , px , py ) , px * w , py * h ,
                            w , h , deviceWidth , deviceHeight );
                }
            }
        }
        mi.destroyImages();
    }

    protected double getMaxResolution( ImageInfo info, int deviceWidth, int deviceHeight, int maxFactor ) {
        double maxResolution;
        if ( info.getUnitWidth() * deviceHeight > info.getUnitHeight() * deviceWidth ) {
            maxResolution = deviceWidth / info.getUnitWidth() * maxFactor;
        } else {
            maxResolution = deviceHeight / info.getUnitHeight() * maxFactor;
        }
        return maxResolution;
    }

    protected void createThumbnail( String outDir , MagickImage image ,
            ImageInfo info , int page ) throws MagickException {
        double resolution = THUMBNAIL_SIZE / Math.max( info.getUnitWidth() , info.getUnitHeight() );

        int w = ( int ) Math.round( info.getUnitWidth() * resolution );
        int h = ( int ) Math.round( info.getUnitHeight() * resolution );

        MagickImage mi = addBorder( image , w , h , w , h );
        cropAndResize( mi , String.format( "%sthumb-%d.jpg" , outDir , page ) ,
                0 , 0 , w , h , w , h );
        mi.destroyImages();

    }

    protected void cropAndResize( MagickImage image , String destPath , int x , int y , int cropWidth , int cropHeight ,
            int resizeWidth , int resizeHeight ) throws MagickException {
    	Rectangle crop = new Rectangle( x , y , cropWidth , cropHeight );
        MagickImage croped = image.cropImage( crop );
        MagickImage scale = croped.scaleImage( resizeWidth, resizeHeight );
        croped.destroyImages();

        scale.setFileName( destPath );
        scale.writeImage( new magick.ImageInfo() );
        scale.destroyImages();
        
        LOGGER.debug( "Convert image:" + destPath);
    }

    protected int[] getImageSize( String path ) {
        String[] sizes =
                ProcUtil.doProc( String.format( "%s -ping %s" , prop.identify , path ) ).split( " " )[ 2 ].split( "x" );
        return new int[] { Integer.parseInt( sizes[ 0 ] ) , Integer.parseInt( sizes[ 1 ] ) };
    }

    protected int getPages( String pdfPath ) {
        Matcher matcher =
                Pattern.compile( "Pages: +([0-9]+)" ).matcher(
                        ProcUtil.doProc( String.format( "%s %s" , prop.pdfInfo , pdfPath ) , true ) );

        if ( !matcher.find() ) {
            throw new RuntimeException( "No pages found" );
        }

        return Integer.parseInt( matcher.group( 1 ) );
    }

    protected String getPpmPath( String prefix , int page ) {
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
