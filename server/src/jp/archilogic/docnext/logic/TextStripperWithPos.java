package jp.archilogic.docnext.logic;

import java.io.IOException;
import java.util.List;

import jp.archilogic.docnext.dto.Region;

import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import com.google.common.collect.Lists;

public class TextStripperWithPos extends PDFTextStripper {
    private final StringBuilder _text = new StringBuilder();
    private final List< Region > _regions = Lists.newArrayList();
    private final float _width;
    private final float _height;

    public TextStripperWithPos( float width , float height ) throws IOException {
        super();

        _width = width;
        _height = height;
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
            _regions.add( new Region( x / _width , ( text.getY() - text.getHeight() ) / _height , w / _width , text
                    .getHeight() / _height ) );
            x += w;
        }
    }
}
