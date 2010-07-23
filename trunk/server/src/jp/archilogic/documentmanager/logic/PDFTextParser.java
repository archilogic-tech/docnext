package jp.archilogic.documentmanager.logic;

import java.io.IOException;
import java.util.List;

import jp.archilogic.documentmanager.dto.PageTextInfo;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class PDFTextParser {
    public List< PageTextInfo > parse( String path ) {
        try {
            PDDocument document = PDDocument.load( path );
            if ( document.isEncrypted() ) {
                document.decrypt( "" );
            }

            List< PageTextInfo > ret = Lists.newArrayList();

            List< ? > allPages = document.getDocumentCatalog().getAllPages();
            for ( int i = 0 ; i < allPages.size() ; i++ ) {
                PDPage page = ( PDPage ) allPages.get( i );
                TextStripperWithPos stripper =
                        new TextStripperWithPos( page.getTrimBox().getWidth() , page.getTrimBox().getHeight() );

                PDStream contents = page.getContents();
                if ( contents != null ) {
                    stripper.processStream( page , page.findResources() , page.getContents().getStream() );
                }

                ret.add( new PageTextInfo( stripper.getText() , stripper.getRegions() ) );
            }

            document.close();

            return ret;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        } catch ( CryptographyException e ) {
            throw new RuntimeException( e );
        } catch ( InvalidPasswordException e ) {
            throw new RuntimeException( e );
        }
    }
}