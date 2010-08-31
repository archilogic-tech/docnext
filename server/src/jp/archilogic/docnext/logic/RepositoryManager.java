package jp.archilogic.docnext.logic;

import jp.archilogic.docnext.bean.PropBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryManager {
    @Autowired
    private PropBean prop;

    public String getImagePath( String type , long documentId , int page , int level , int px , int py ) {
        return String.format( "%s/thumb/%d/%s%d-%d-%d-%d.jpg" , prop.repository , documentId , type , page , level ,
                px , py );
    }
}
