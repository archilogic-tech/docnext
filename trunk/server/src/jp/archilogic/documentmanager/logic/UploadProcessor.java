package jp.archilogic.documentmanager.logic;

import java.io.File;

import jp.archilogic.documentmanager.bean.PropBean;
import jp.archilogic.documentmanager.dao.DocumentDao;
import jp.archilogic.documentmanager.dto.PageTextInfo;
import jp.archilogic.documentmanager.dto.TOCElem;
import jp.archilogic.documentmanager.entity.Document;
import jp.archilogic.documentmanager.exception.UnsupportedFormatException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

        @Override
        public void run() {
            try {
                FileUtils.copyFile( new File( tempPath ) , new File( prop.repository + "/raw/" + doc.id ) );

                String tempPdfPath = saveAsPdf( doc.fileName , tempPath , doc.id );

                double ratio =
                        thumbnailCreator.create( prop.repository + "/thumb/" + doc.id + "/" , tempPdfPath , "ppm"
                                + doc.id );

                int pages = thumbnailCreator.getPages( tempPdfPath );

                packManager.createStruct( doc.id );
                packManager.copyThumbnails( doc.id );
                packManager.writePages( doc.id , pages );
                packManager.writeTOC( doc.id , Lists.< TOCElem > newArrayList() );
                packManager.writeSinglePageInfo( doc.id , Lists.< Integer > newArrayList() );
                packManager.writeRatio( doc.id , ratio );

                int page = 0;
                for ( PageTextInfo pageTextInfo : pdfTextParser.parse( tempPdfPath ) ) {
                    packManager.writeText( doc.id , page , String.format( "<t>%s</t>" , pageTextInfo.text ) );
                    packManager.writeTextInfo( doc.id , page , pageTextInfo );

                    page++;
                }

                packManager.repack( doc.id );

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
                    String tempPdfPath = "temp" + documentId + ".pdf";
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

    public void proc( String tempPath , Document doc ) {
        new UploadTask( tempPath , doc ).run();
    }
}
