package jp.archilogic.docnext.android.meta;

import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.image.CoreImageView;
import jp.archilogic.docnext.android.coreview.text.CoreTextView;
import jp.archilogic.docnext.android.thumnail.ThumnailView;
import jp.archilogic.docnext.android.toc.TOCView;
import android.content.Context;

public enum DocumentType {
    IMAGE , TEXT , HOME , TOC , BOOKMARK , THUMNAIL , SETTING , COMMENT , SEARCH;

    public CoreView buildView( final Context context ) {
        switch ( this ) {
        case IMAGE:
            return new CoreImageView( context );
        case TEXT:
            return new CoreTextView( context );
        case THUMNAIL:
            return new ThumnailView( context );
        case TOC:
            return new TOCView( context );
        default:
            throw new RuntimeException();
        }
    }
}
