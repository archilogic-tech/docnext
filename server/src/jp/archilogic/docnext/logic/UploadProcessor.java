package jp.archilogic.docnext.logic;

import java.io.File;
import java.util.List;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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

        @Override
        public void run() {
            try {
                progressManager.setStep( doc.id , Step.INITIALIZING );

                FileUtils.copyFile( new File( tempPath ) , new File( prop.repository + "/raw/" + doc.id ) );

                final String tempPdfPath = saveAsPdf( doc.fileName , tempPath , doc.id );
                final String ppmPath = FileUtil.createSameDirPath( tempPath , "ppm" );

                try {
                    pdfAnnotationParser.checkCanParse( tempPdfPath );
                } catch ( final MalformedPDFException e ) {
                    progressManager.setError( doc.id , ErrorType.MALFORMED );
                    return;
                } catch ( final EncryptedPDFException e ) {
                    progressManager.setError( doc.id , ErrorType.ENCRYPTED );
                    return;
                }

                packManager.createStruct( doc.id );

                parseAnnotation( tempPdfPath );

                final String cleanedPath = FilenameUtils.getFullPathNoEndSeparator( tempPdfPath ) + File.separator + //
                        "cleaned" + doc.id + ".pdf";
                pdfAnnotationParser.clean( tempPdfPath , cleanedPath );

                progressManager.setTotalThumbnail( doc.id , thumbnailCreator.getPages( cleanedPath ) );
                progressManager.setStep( doc.id , Step.CREATING_THUMBNAIL );

                final CreateResult res = thumbnailCreator.create( prop.repository + "/thumb/" + doc.id + "/" , //
                        cleanedPath , ppmPath + doc.id , doc.id );

                packManager.copyThumbnails( doc.id );
                packManager.writePages( doc.id , thumbnailCreator.getPages( cleanedPath ) );
                packManager.writeTOC( doc.id , Lists.newArrayList( new TOCElem( 0 , "Chapter" ) ) );
                packManager.writeSinglePageInfo( doc.id , Lists.< Integer > newArrayList() );
                packManager.writeImageCreateResult( doc.id , res );

                parseText( cleanedPath );

                packManager.repack( doc.id );

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
