package jp.archilogic.docnext.android.info;

public class BookmarkInfo implements Comparable< BookmarkInfo > {
    public int page;
    public String text;

    public BookmarkInfo() {
    }
    
    public BookmarkInfo( int argumentPage ) {
        text = "";
        page = argumentPage;
    }
    
    public BookmarkInfo( String string , int argumentPage ) {
        text = string;
        page = argumentPage;
    }

    @Override
    public int compareTo( BookmarkInfo another ) {
        return page - another.page;
    }
    
    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof BookmarkInfo ) {
            return obj == null ? false : ( ( BookmarkInfo )obj ).page == page;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return page;
    }
}
