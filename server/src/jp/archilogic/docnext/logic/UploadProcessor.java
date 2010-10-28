package jp.archilogic.docnext.logic;

import java.io.File;
import java.util.List;

import jp.archilogic.docnext.bean.PropBean;
import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.dto.TOCElem;
import jp.archilogic.docnext.entity.Document;
import jp.archilogic.docnext.exception.UnsupportedFormatException;
import jp.archilogic.docnext.logic.PDFAnnotationParser.PageAnnotationInfo;
import jp.archilogic.docnext.logic.PDFTextParser.PageTextInfo;
import jp.archilogic.docnext.logic.ProgressManager.Step;
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

        public UploadTask( String tempPath , Document doc ) {
            this.tempPath = tempPath;
            this.doc = doc;
        }

        private void parseAnnotation( String tempPdfPath ) {
            int page = 0;
            for ( List< PageAnnotationInfo > infos : pdfAnnotationParser.parse( tempPdfPath ) ) {
                packManager.writeAnnotations( doc.id , page , infos );

                page++;
            }
        }

        private void parseText( String cleanedPath ) {
            int page = 0;
            for ( PageTextInfo pageTextInfo : pdfTextParser.parse( cleanedPath ) ) {
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

                String tempPdfPath = saveAsPdf( doc.fileName , tempPath , doc.id );
                String ppmPath = FileUtil.createSameDirPath( tempPath , "ppm" );

                packManager.createStruct( doc.id );

                parseAnnotation( tempPdfPath );

                String cleanedPath =
                        FilenameUtils.getFullPathNoEndSeparator( tempPdfPath ) + File.separator + "cleaned" + doc.id
                                + ".pdf";
                pdfAnnotationParser.clean( tempPdfPath , cleanedPath );

                progressManager.setTotalThumbnail( doc.id , thumbnailCreator.getPages( cleanedPath ) );
                progressManager.setStep( doc.id , Step.CREATING_THUMBNAIL );

                double ratio =
                        thumbnailCreator.create( prop.repository + "/thumb/" + doc.id + "/" , cleanedPath , ppmPath
                                + doc.id , doc.id );

                packManager.copyThumbnails( doc.id );
                packManager.writePages( doc.id , thumbnailCreator.getPages( cleanedPath ) );
                packManager.writeTOC( doc.id , Lists.newArrayList( new TOCElem( 0 , "Chapter" ) ) );
                packManager.writeSinglePageInfo( doc.id , Lists.< Integer > newArrayList() );
                packManager.writeRatio( doc.id , ratio );

                parseText( cleanedPath );

                packManager.repack( doc.id );

                progressManager.clearCompleted( doc.id );

                doc.processing = false;
                documentDao.update( doc );
            } catch ( Throwable e ) {
                throw new RuntimeException( e );
            }
        }

        private String saveAsPdf( String path , String tempPath , long documentId ) {
            try {
                if ( FilenameUtils.getExtension( path ).equals( "pdf" ) ) {
                    return tempPath;
                } else {
                    String tempDir = FilenameUtils.getFullPathNoEndSeparator( tempPath );
                    String tempPdfPath = tempDir + File.separator + "temp" + documentId + ".pdf";
                    converter.convert( new File( tempPath ) , new File( tempPdfPath ) );
                    return tempPdfPath;
                }
            } catch ( OpenOfficeException e ) {
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

    public void proc( String tempPath , Document doc ) {
        taskExecutor.execute( new UploadTask( tempPath , doc ) );
    }
}
