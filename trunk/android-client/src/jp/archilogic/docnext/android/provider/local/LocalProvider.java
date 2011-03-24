package jp.archilogic.docnext.android.provider.local;

import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.info.ImageInfo;
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

    int getImageLevel( long id );

    /**
     * @return null if not exists
     */
    String getImagePath( long id , int page , int level , int px , int py );

    /**
     * more partial? (for large text)
     * 
     * @return null if not exists
     */
    TextInfo getTextInfo( long id , int page );

    boolean isCompleted( long id );

    boolean isImageExists( long id , int page );

    void setCompleted( long id );
}
