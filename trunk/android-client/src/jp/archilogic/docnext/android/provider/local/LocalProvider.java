package jp.archilogic.docnext.android.provider.local;

import java.util.List;

import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
import jp.archilogic.docnext.android.info.TOCElement;
import jp.archilogic.docnext.android.info.TextInfo;

/**
 * This interface may be change (for requested feature)
 */
public interface LocalProvider {
    /**
     * @return null if not exists
     */
    DocInfo getDocInfo( long id );

    /**
     * @return null if not exists
     */
    String getFontPath( String name );

    /**
     * @return null if not exists
     */
    ImageInfo getImageInfo( long id );

    /**
     * @return null if not exists
     */
    String getImagePath( long id , int page , int level , int px , int py );

    /**
     * @return null if not exists
     */
	List <TOCElement> getTableOfContentsInfo(long _id);
	
    /**
     * more partial? (for large text)
     * 
     * @return null if not exists
     */
    TextInfo getText( long id , int page );

    boolean isCompleted( long id );

    boolean isImageExists( long id , int page );

    void setCompleted( long id );

}