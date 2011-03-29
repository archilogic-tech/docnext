package jp.archilogic.docnext.android.meta;

import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.image.CoreImageView;
import jp.archilogic.docnext.android.coreview.text.CoreTextView;
import android.content.Context;

public enum CoreViewType {
    IMAGE , TEXT;

    public CoreView buildView( final Context context ) {
        switch ( this ) {
        case IMAGE:
            return new CoreImageView( context );
        case TEXT:
            return new CoreTextView( context );
        default:
            throw new RuntimeException();
        }
    }
}
