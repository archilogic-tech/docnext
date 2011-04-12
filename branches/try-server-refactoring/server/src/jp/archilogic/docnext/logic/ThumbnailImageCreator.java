package jp.archilogic.docnext.logic;

import org.springframework.stereotype.Component;

@Component
public class ThumbnailImageCreator extends ThumbnailCreator implements ImageCreator {

	@Override
    public CreateResult create( String outDir , String pdfPath , String prefix, long id ) {
        return super.create(outDir, pdfPath, prefix, id);
    }
    
    @Override
    public int getPages( String pdfPath ) {
        return super.getPages(pdfPath);
    }

}
