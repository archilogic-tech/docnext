package jp.archilogic.docnext.android.type;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public enum FragmentType {
    HOME , IMAGE , TEXT , TOC , BOOKMARK , THUMBNAIL , SETTING , COMMENT , SEARCH , BOOKMARKLIST;

    public View buildSwithButton( final Context context ) {
        final LinearLayout root = new LinearLayout( context );
        root.setLayoutParams( new LinearLayout.LayoutParams( 0 ,
                LinearLayout.LayoutParams.WRAP_CONTENT , 1 ) );
        root.setOrientation( LinearLayout.VERTICAL );
        root.setGravity( Gravity.CENTER_HORIZONTAL );

        final ImageView image = new ImageView( context );
        image.setId( R.id.bookmark );
        image.setImageResource( getImageResouce() );
        root.addView( image );

        final TextView text = new TextView( context );
        text.setGravity( Gravity.CENTER_HORIZONTAL );
        text.setText( getTextResource() );
        text.setTextColor( Color.WHITE );
        root.addView( text );

        return root;
    }

    public DocumentType getDocumentType() {
        switch ( this ) {
        case IMAGE:
            return DocumentType.IMAGE;
        case TEXT:
            return DocumentType.TEXT;
        case TOC:
            return DocumentType.TOC;
        case THUMBNAIL:
            return DocumentType.THUMBNAIL;
        case BOOKMARKLIST:
            return DocumentType.BOOKMARK;
        case HOME:
        case SETTING:
        case COMMENT:
        case SEARCH:
        case BOOKMARK:
            return null;
        default:
            throw new RuntimeException();
        }
    }

    private int getImageResouce() {
        switch ( this ) {
        case HOME:
            return R.drawable.button_home;
        case IMAGE:
            return R.drawable.button_image;
        case TEXT:
            return R.drawable.button_text;
        case TOC:
            return R.drawable.button_toc;
        case BOOKMARK:
            return R.drawable.button_bookmark_off;
        case BOOKMARKLIST:
            return R.drawable.button_bookmarklist;
        case THUMBNAIL:
            return R.drawable.button_thumbnail;
        case SETTING:
            return R.drawable.button_setting;
        case COMMENT:
            return R.drawable.button_comment;
        case SEARCH:
            return R.drawable.button_search;
        default:
            throw new RuntimeException();
        }
    }

    private int getTextResource() {
        switch ( this ) {
        case HOME:
            return R.string.home;
        case IMAGE:
            return R.string.image;
        case TEXT:
            return R.string.text;
        case TOC:
            return R.string.toc;
        case BOOKMARK:
            return R.string.bookmark;
        case THUMBNAIL:
            return R.string.thumbnail;
        case SETTING:
            return R.string.setting;
        case COMMENT:
            return R.string.comment;
        case SEARCH:
            return R.string.search;
        case BOOKMARKLIST:
            return R.string.bookmark_list;
        default:
            throw new RuntimeException();
        }
    }
}
