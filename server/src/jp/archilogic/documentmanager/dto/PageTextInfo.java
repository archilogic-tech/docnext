package jp.archilogic.documentmanager.dto;

import java.util.List;

public class PageTextInfo {
    public String text;
    public List< Region > regions;

    public PageTextInfo( String text , List< Region > regions ) {
        this.text = text;
        this.regions = regions;
    }
}
