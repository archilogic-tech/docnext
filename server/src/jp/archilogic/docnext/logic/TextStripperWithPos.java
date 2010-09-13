package jp.archilogic.docnext.logic;

import java.io.IOException;
import java.util.List;

import jp.archilogic.docnext.dto.Region;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import com.google.common.collect.Lists;

public class TextStripperWithPos extends PDFTextStripper {
    private final StringBuilder _text = new StringBuilder();
    private final List< Region > _regions = Lists.newArrayList();
    private final PDRectangle _trim;
    private final PDRectangle _media;

    public TextStripperWithPos( PDRectangle trim , PDRectangle media ) throws IOException {
        super();

        _trim = trim;
        _media = media;
    }

    public List< Region > getRegions() {
        return _regions;
    }

    public String getText() {
        return _text.toString();
    }

    @Override
    protected void processTextPosition( TextPosition text ) {
        _text.append( text.getCharacter() );

        if ( text.getCharacter().length() != text.getIndividualWidths().length ) {
            throw new RuntimeException( "Invalid TextPosition" );
        }

        float x = text.getX();
        for ( int index = 0 ; index < text.getCharacter().length() ; index++ ) {
            float w = text.getIndividualWidths()[ index ];
            // fix y for coordinate system
            _regions.add( new Region( ( x - _trim.getLowerLeftX() ) / _trim.getWidth() , ( text.getY()
                    - ( _media.getHeight() - _trim.getUpperRightY() ) - text.getHeight() )
                    / _trim.getHeight() , w / _trim.getWidth() , text.getHeight() / _trim.getHeight() ) );
            x += w;
        }
    }
}
