package jp.archilogic.docnext.logic;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class PDFAnnotationCleaner {
    public void clean( String src , String dst ) {
        try {
            PDDocument document = PDDocument.load( src );
            if ( document.isEncrypted() ) {
                document.decrypt( "" );
            }

            List< ? > allPages = document.getDocumentCatalog().getAllPages();
            for ( int i = 0 ; i < allPages.size() ; i++ ) {
                PDPage page = ( PDPage ) allPages.get( i );

                page.setAnnotations( Lists.newArrayList() );
            }

            document.save( dst );
            document.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        } catch ( CryptographyException e ) {
            throw new RuntimeException( e );
        } catch ( InvalidPasswordException e ) {
            throw new RuntimeException( e );
        } catch ( COSVisitorException e ) {
            throw new RuntimeException( e );
        }
    }
}
