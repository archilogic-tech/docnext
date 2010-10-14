package jp.archilogic.docnext.logic;

import java.awt.Dimension;
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

        return new ImageInfo( width , height , pages );
    }

    /**
     * 指定した幅、高さの縦横比を維持できるように枠を付加する
     */
    protected MagickImage addBorder( MagickImage image , int width , int height ) throws MagickException {
        LOGGER.debug("Add border:"+width+","+height);

        Dimension d = image.getDimension();
        int iw = (int) d.width;
        int ih = (int) d.height;

        Rectangle border;
        MagickImage scaled;
        if ( width * ih >= height * iw ) {
            int nw = ( iw * height / ih ) / 2 * 2;  // 偶数化
            scaled = image.scaleImage( nw , height );
            border = new Rectangle( ( width  - nw ) / 2 , 0 );
        } else {
            int nh = ( ih * width / iw ) / 2 * 2;  // 偶数化
            scaled = image.scaleImage( width , nh );
            border = new Rectangle( 0 , ( height  - nh ) / 2 );
        }
        MagickImage bordered = scaled.borderImage( border );
        scaled.destroyImages();

        return bordered;
    }

    /**
     * @return width / height ratio
     */
    public ImageInfo create( String outDir , String pdfPath , String prefix ) {
        LOGGER.info( "Begin create image" );
        long t = System.currentTimeMillis();

        new File( outDir ).mkdir();

        ImageInfo info = createAllPpms( pdfPath , prefix );
        createAllPages(outDir, prefix, info);

        LOGGER.info( "End create image. Tooks " + ( System.currentTimeMillis() - t ) + "(ms)" );

        return info;
    }

    protected void createAllPages( String outDir, String prefix, ImageInfo info ) {
        try {
            for ( int page = 0 , pages = info.getPages() ; page < pages ; page++ ) {
                LOGGER.info( "Proc page: " + page );
                MagickImage mi = new MagickImage(new magick.ImageInfo( getPpmPath(prefix, page)));

                createImage( outDir + "iPad" , mi , page , IPAD_MAX_LEVEL , IPAD_DEVICE_WIDTH ,
                        IPAD_DEVICE_HEIGHT );
                createImage( outDir + "iPhone" , mi , page , IPHONE_MAX_LEVEL , IPHONE_DEVICE_WIDTH ,
                        IPHONE_DEVICE_HEIGHT );
                createThumbnail( outDir , mi , page );
            }
        } catch (MagickException e) {
        	throw new RuntimeException("Can't create image.", e);
        }
    }

    protected void createImage( String outPath , MagickImage image , int page ,
            int maxLevel , int deviceWidth , int deviceHeight ) throws MagickException {
        for ( int level = 0 ; level < maxLevel ; level++ ) {
            LOGGER.info( "Proc level: " + level );

            int factor = ( int ) Math.pow( 2 , level );
            MagickImage mi = addBorder(image , deviceWidth * factor, deviceHeight * factor );

            for ( int py = 0 ; py < factor ; py++ ) {
                for ( int px = 0 ; px < factor ; px++ ) {
                    LOGGER.info( "Proc part: " + px + "," + py );

                    String file = String.format( "%s%d-%d-%d-%d.jpg" , outPath , page , level , px , py );
                    cropImage( mi , file , px * deviceWidth , py * deviceHeight ,
                               deviceWidth , deviceHeight );
                }
            }
            mi.destroyImages();
        }
    }

    protected void createThumbnail( String outDir , MagickImage image , int page ) throws MagickException {
        Dimension d = image.getDimension();
        double ratio = THUMBNAIL_SIZE / (double) Math.max( (int) d.width , (int) d.height );
        int w = ( int ) Math.round( (int) d.width * ratio );
        int h = ( int ) Math.round( (int) d.height * ratio );

        MagickImage scaled = image.scaleImage( w, h );
        scaled.setFileName( String.format( "%sthumb-%d.jpg" , outDir , page ) );
        scaled.writeImage( new magick.ImageInfo() );
        scaled.destroyImages();

    }

    protected void cropImage( MagickImage image , String destPath , int x , int y ,
            int cropWidth , int cropHeight ) throws MagickException {
        LOGGER.debug( "CROP :" + x + "," + y + "," + cropWidth + "," + cropHeight );

        Rectangle crop = new Rectangle( x , y , cropWidth , cropHeight );
        MagickImage croped = image.cropImage( crop );

        croped.setFileName( destPath );
        croped.writeImage( new magick.ImageInfo() );
        croped.destroyImages();

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
