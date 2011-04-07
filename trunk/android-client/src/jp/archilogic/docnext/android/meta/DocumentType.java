package jp.archilogic.docnext.android.meta;

import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.image.CoreImageView;
import jp.archilogic.docnext.android.coreview.text.CoreTextView;
import jp.archilogic.docnext.android.thumnail.ThumnailView;
import jp.archilogic.docnext.android.toc.TOCView;
import jp.archilogic.docnext.android.type.FragmentType;
import android.content.Context;

public enum DocumentType {
    IMAGE , TEXT , TOC , THUMNAIL;

    public CoreView buildView( final Context context ) {
        switch ( this ) {
        case IMAGE:
            return new CoreImageView( context );
        case TEXT:
            return new CoreTextView( context );
        case TOC:
            return new TOCView( context );
        case THUMNAIL:
            return new ThumnailView( context );
        default:
            throw new RuntimeException();
        }
    }

    public FragmentType[] getPrimarySwitchFragment() {
        switch ( this ) {
        case IMAGE:
            return new FragmentType[] { FragmentType.TEXT , FragmentType.TOC , FragmentType.BOOKMARK ,
                    FragmentType.THUMNAIL };
        case TEXT:
            return new FragmentType[] { FragmentType.IMAGE , FragmentType.TOC , FragmentType.BOOKMARK ,
                    FragmentType.THUMNAIL };
        case TOC:
            return new FragmentType[] { FragmentType.IMAGE , FragmentType.TEXT , FragmentType.BOOKMARK ,
                    FragmentType.THUMNAIL };
        case THUMNAIL:
            return new FragmentType[] { FragmentType.IMAGE , FragmentType.TEXT , FragmentType.TOC ,
                    FragmentType.BOOKMARK };
        default:
            return null;
        }
    }

    public FragmentType[] getSecondarySwitchFragment() {
        return new FragmentType[] { FragmentType.HOME , FragmentType.SETTING , FragmentType.COMMENT };
    }

    public FragmentType[] getSubSecondarySwitchFragment() {
        return new FragmentType[] { FragmentType.SEARCH };
    }
}
