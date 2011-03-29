package jp.archilogic.docnext.dto;

import jp.archilogic.docnext.type.DocumentType;

public class DocInfo {
    public long id;
    public DocumentType[] types;
    public int pages;

    public DocInfo( final long id , final DocumentType[] types , final int pages ) {
        this.id = id;
        this.types = types;
        this.pages = pages;
    }
}
