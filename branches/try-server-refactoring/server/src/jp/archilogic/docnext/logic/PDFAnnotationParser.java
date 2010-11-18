package jp.archilogic.docnext.logic;

import java.io.IOException;
import java.util.List;

import jp.archilogic.docnext.dto.Region;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class PDFAnnotationParser {
    interface Action {
    }

    class GoToPageAction implements Action {
        public String action = "GoToPage";
        public int page;

        GoToPageAction( int page ) {
            this.page = page;
        }
    }

    class PageAnnotationInfo {
        public Region region;
        public Action action;

        public PageAnnotationInfo( Region region , Action action ) {
            this.region = region;
            this.action = action;
        }
    }

    class URIAction implements Action {
        public String action = "URI";
        public String uri;

        URIAction( String uri ) {
            this.uri = uri;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger( PDFAnnotationParser.class );

    public boolean canParse( String src ) {
        try {
            PDDocument document = PDDocument.load( src );

            if ( document.isEncrypted() ) {
                document.openProtection( new StandardDecryptionMaterial( null ) );
            }

            return true;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        } catch ( BadSecurityHandlerException e ) {
            throw new RuntimeException( e );
        } catch ( CryptographyException e ) {
            // the document has a password, which length > 0
            return false;
        }
    }

    public void clean( String src , String dst ) {
        try {
            PDDocument document = PDDocument.load( src );

            if ( document.isEncrypted() ) {
                document.openProtection( new StandardDecryptionMaterial( null ) );
                document.setAllSecurityToBeRemoved( true );
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
        } catch ( COSVisitorException e ) {
            throw new RuntimeException( e );
        } catch ( BadSecurityHandlerException e ) {
            throw new RuntimeException( e );
        } catch ( CryptographyException e ) {
            throw new RuntimeException( e );
        }
    }

    private Region convertToRegion( PDAnnotation anno , PDPage page ) {
        PDRectangle rect = anno.getRectangle();
        PDRectangle crop = page.findCropBox();
        PDRectangle media = page.findMediaBox();

        PDRectangle container = crop.getWidth() < media.getWidth() ? crop : media;

        float w = container.getWidth();
        float h = container.getHeight();

        return new Region( ( rect.getLowerLeftX() - container.getLowerLeftX() ) / w ,
                ( h - ( rect.getUpperRightY() - container.getLowerLeftY() ) ) / h , rect.getWidth() / w ,
                rect.getHeight() / h );
    }

    @SuppressWarnings( "unchecked" )
    private List< PageAnnotationInfo > getAnnotations( PDPage page ) throws IOException {
        List< PageAnnotationInfo > ret = Lists.newArrayList();

        for ( PDAnnotation anno : ( List< PDAnnotation > ) page.getAnnotations() ) {
            if ( anno instanceof PDAnnotationLink ) {
                PDAction action = ( ( PDAnnotationLink ) anno ).getAction();

                if ( action instanceof PDActionGoTo ) {
                    PDDestination dest = ( ( PDActionGoTo ) action ).getDestination();

                    if ( dest instanceof PDNamedDestination ) {
                        LOGGER.info( "PDNamedDestination is not supported" );
                    } else if ( dest instanceof PDPageDestination ) {
                        ret.add( new PageAnnotationInfo( convertToRegion( anno , page ) , new GoToPageAction(
                                ( ( PDPageDestination ) dest ).findPageNumber() ) ) );
                    } else {
                        LOGGER.info( "Unsupported PDDestination: " + nsGetClassName( dest ) );
                    }
                } else if ( action instanceof PDActionURI ) {
                    ret.add( new PageAnnotationInfo( convertToRegion( anno , page ) , new URIAction(
                            ( ( PDActionURI ) action ).getURI() ) ) );
                } else {
                    LOGGER.info( "Unsupported PDActionGoto: " + nsGetClassName( action ) );
                }
            } else {
                LOGGER.info( "Unsupported PDAnnotation: " + nsGetClassName( anno ) );
            }
        }

        return ret;
    }

    private String nsGetClassName( Object object ) {
        return object != null ? object.getClass().toString() : "(null)";
    }

    public List< List< PageAnnotationInfo >> parse( String src ) {
        try {
            PDDocument document = PDDocument.load( src );
            if ( document.isEncrypted() ) {
                document.openProtection( new StandardDecryptionMaterial( null ) );
            }

            List< List< PageAnnotationInfo >> ret = Lists.newArrayList();

            List< ? > allPages = document.getDocumentCatalog().getAllPages();
            for ( int i = 0 ; i < allPages.size() ; i++ ) {
                ret.add( getAnnotations( ( PDPage ) allPages.get( i ) ) );
            }

            document.close();

            return ret;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        } catch ( BadSecurityHandlerException e ) {
            throw new RuntimeException( e );
        } catch ( CryptographyException e ) {
            throw new RuntimeException( e );
        }
    }
}
