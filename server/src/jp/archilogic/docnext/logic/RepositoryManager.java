package jp.archilogic.docnext.logic;

import jp.archilogic.docnext.bean.PropBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryManager {
    @Autowired
    private PropBean prop;

    public String getFontPath( final String name ) {
        return String.format( "%s/font/%s" , prop.repository , name );
    }

    public String getImagePath( final long id , final int page , final int level , final int px , final int py ) {
        return String.format( "%s/thumb/%d/texture%d-%d-%d-%d.jpg" , prop.repository , id , page , level , px , py );
    }

    public String getImagePath( final String type , final long docId , final int page ) {
        return String.format( "%s/thumb/%d/%s-%d.jpg" , prop.repository , docId , type , page );
    }

    public String getImagePath( final String type , final long docId , final int page , final int level , final int px ,
            final int py ) {
        return String
                .format( "%s/thumb/%d/%s%d-%d-%d-%d.jpg" , prop.repository , docId , type , page , level , px , py );
    }

    public String getTextPath( final long id , final int page ) {
        return String.format( "%s/pack/%d/texts/%d.json" , prop.repository , id , page );
    }

    public String getThumbnailPath( long id , int page ) {
        return String.format( "%s/thumb/%d/thumbnail%d.jpg" , prop.repository , id , page );
    }
}
