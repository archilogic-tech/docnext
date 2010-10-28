package jp.archilogic.docnext.logic;

import jp.archilogic.docnext.bean.PropBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryManager {
    @Autowired
    private PropBean prop;

    public String getImagePath( String type , long docId , int page ) {
        return String.format( "%s/thumb/%d/%s-%d.jpg" , prop.repository , docId , type , page );
    }

    public String getImagePath( String type , long docId , int page , int level , int px , int py ) {
        return String.format( "%s/thumb/%d/%s%d-%d-%d-%d.jpg" , prop.repository , docId , type , page , level ,
                px , py );
    }
}
