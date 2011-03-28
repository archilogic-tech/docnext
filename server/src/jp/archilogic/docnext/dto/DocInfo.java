package jp.archilogic.docnext.dto;

public class DocInfo {
    public long id;
    public int type;
    public int pages;

    public DocInfo( final long id , final int type , final int pages ) {
        this.id = id;
        this.type = type;
        this.pages = pages;
    }
}
