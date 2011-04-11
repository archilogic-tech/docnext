package jp.archilogic.docnext.android.coreview;

public interface HasPage {
    String BROADCAST_PAGE_CHANGED = HasPage.class.getName() + ".page.changed";

    int getPage();

    void setPage( int page );
}
