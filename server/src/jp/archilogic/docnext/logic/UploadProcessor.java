package jp.archilogic.docnext.logic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;

import jp.archilogic.docnext.bean.PropBean;
import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.dto.TOCElem;
import jp.archilogic.docnext.entity.Document;
import jp.archilogic.docnext.exception.EncryptedPDFException;
import jp.archilogic.docnext.exception.MalformedPDFException;
import jp.archilogic.docnext.exception.UnsupportedFormatException;
import jp.archilogic.docnext.logic.PDFAnnotationParser.PageAnnotationInfo;
import jp.archilogic.docnext.logic.PDFTextParser.PageTextInfo;
import jp.archilogic.docnext.logic.ProgressManager.ErrorType;
import jp.archilogic.docnext.logic.ProgressManager.Step;
import jp.archilogic.docnext.logic.ThumbnailCreator.CreateResult;
import jp.archilogic.docnext.util.FileUtil;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeException;
import com.google.common.collect.Lists;
import com.sun.star.task.ErrorCodeIOException;

@Component
public class UploadProcessor {
    private class UploadTask implements Runnable {
        private final String tempPath;
        private final Document doc;

        public UploadTask( final String tempPath , final Document doc ) {
            this.tempPath = tempPath;
            this.doc = doc;
        }

        private boolean isSupportedImage( final String path ) {
            final String[] WHITE_LIST = { ".jpg" , ".png" , ".bmp" , ".gif" };

            for ( final String ext : WHITE_LIST ) {
                if ( path.endsWith( ext ) ) {
                    return true;
                }
            }

            return false;
        }

        private void parseAnnotation( final String tempPdfPath ) {
            int page = 0;
            for ( final List< PageAnnotationInfo > infos : pdfAnnotationParser.parse( tempPdfPath ) ) {
                packManager.writeAnnotations( doc.id , page , infos );

                page++;
            }
        }

        private void parseText( final String cleanedPath ) {
            int page = 0;
            for ( final PageTextInfo pageTextInfo : pdfTextParser.parse( cleanedPath ) ) {
                packManager.writeText( doc.id , page , String.format( "<t>%s</t>" , pageTextInfo.text ) );
                packManager.writeImageText( doc.id , page , pageTextInfo.text );
                packManager.writeRegions( doc.id , page , pageTextInfo.regions );

                page++;
            }
        }

        private boolean procArchive() throws Exception {
            packManager.createStruct( doc.id );

            final ZipFile file = new ZipFile( tempPath );

            final List< String > paths = Lists.newArrayList();
            for ( final Enumeration< ? > e = file.getEntries() ; e.hasMoreElements() ; ) {
                final ZipArchiveEntry entry = ( ZipArchiveEntry ) e.nextElement();

                if ( !entry.isDirectory() && isSupportedImage( entry.getName() ) ) {
                    paths.add( entry.getName() );
                }
            }

            Collections.sort( paths );

            int width = -1;
            int height = -1;

            int page = 0;
            for ( final String path : paths ) {
                final BufferedImage image = ImageIO.read( file.getInputStream( file.getEntry( path ) ) );

                if ( width == -1 && height == -1 ) {
                    width = image.getWidth();
                    height = image.getHeight();
                }

                if ( width != image.getWidth() || height != image.getHeight() ) {
                    progressManager.setError( doc.id , ErrorType.MALFORMED );
                    return false;
                }

                final String tmpPath = prop.tmp + "/tmp.png";

                final OutputStream out = new FileOutputStream( tmpPath );
                ImageIO.write( image , "jpg" , out );
                IOUtils.closeQuietly( out );

                thumbnailCreator.createFromImage( prop.repository + "/thumb/" + doc.id + "/" , tmpPath , page , doc.id );

                page++;
            }

            doc.nLevel =
                    ( int ) Math.floor( Math.log( 1.0 * width / ThumbnailCreator.TEXTURE_SIZE ) / Math.log( 2 ) ) + 1;

            return true;
        }

        private boolean procDocument() {
            final String tempPdfPath = saveAsPdf( doc.fileName , tempPath , doc.id );
            final String ppmPath = FileUtil.createSameDirPath( tempPath , "ppm" );

            try {
                pdfAnnotationParser.checkCanParse( tempPdfPath );
            } catch ( final MalformedPDFException e ) {
                progressManager.setError( doc.id , ErrorType.MALFORMED );
                return false;
            } catch ( final EncryptedPDFException e ) {
                progressManager.setError( doc.id , ErrorType.ENCRYPTED );
                return false;
            }

            packManager.createStruct( doc.id );

            parseAnnotation( tempPdfPath );

            final String cleanedPath = FilenameUtils.getFullPathNoEndSeparator( tempPdfPath ) + File.separator + //
                    "cleaned" + doc.id + ".pdf";
            pdfAnnotationParser.clean( tempPdfPath , cleanedPath );

            progressManager.setTotalThumbnail( doc.id , thumbnailCreator.getPages( cleanedPath ) );
            progressManager.setStep( doc.id , Step.CREATING_THUMBNAIL );

            final CreateResult res = thumbnailCreator.createFromPDF( prop.repository + "/thumb/" + doc.id + "/" , //
                    cleanedPath , ppmPath + doc.id , doc.id );

            packManager.copyThumbnails( doc.id );
            packManager.writePages( doc.id , thumbnailCreator.getPages( cleanedPath ) );
            packManager.writeTOC( doc.id , Lists.newArrayList( new TOCElem( 0 , "Chapter" ) ) );
            packManager.writeSinglePageInfo( doc.id , Lists.< Integer > newArrayList() );
            packManager.writeImageCreateResult( doc.id , res );

            parseText( cleanedPath );

            packManager.repack( doc.id );

            return true;
        }

        @Override
        public void run() {
            try {
                progressManager.setStep( doc.id , Step.INITIALIZING );

                FileUtils.copyFile( new File( tempPath ) , new File( prop.repository + "/raw/" + doc.id ) );

                if ( doc.fileName.endsWith( ".zip" ) ) {
                    if ( !procArchive() ) {
                        return;
                    }
                } else {
                    if ( !procDocument() ) {
                        return;
                    }
                }

                doc.processing = false;
                documentDao.update( doc );
            } catch ( final Throwable e ) {
                throw new RuntimeException( e );
            } finally {
                progressManager.clearCompleted( doc.id );
            }
        }

        private String saveAsPdf( final String path , final String tempPath , final long documentId ) {
            try {
                if ( FilenameUtils.getExtension( path ).equals( "pdf" ) ) {
                    return tempPath;
                } else {
                    final String tempDir = FilenameUtils.getFullPathNoEndSeparator( tempPath );
                    final String tempPdfPath = tempDir + File.separator + "temp" + documentId + ".pdf";
                    converter.convert( new File( tempPath ) , new File( tempPdfPath ) );
                    return tempPdfPath;
                }
            } catch ( final OpenOfficeException e ) {
                // currently, unnecessary code though...
                if ( e.getCause() instanceof ErrorCodeIOException ) {
                    throw new UnsupportedFormatException();
                } else {
                    throw e;
                }
            }
        }
    }

    @Autowired
    private ThumbnailCreator thumbnailCreator;
    @Autowired
    private DocumentConverter converter;
    @Autowired
    private PropBean prop;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private PackManager packManager;
    @Autowired
    private PDFTextParser pdfTextParser;
    @Autowired
    private PDFAnnotationParser pdfAnnotationParser;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private ProgressManager progressManager;

    public void proc( final String tempPath , final Document doc ) {
        taskExecutor.execute( new UploadTask( tempPath , doc ) );
    }
}
