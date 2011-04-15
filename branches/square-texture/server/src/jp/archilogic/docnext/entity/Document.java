package jp.archilogic.docnext.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import jp.archilogic.docnext.type.DocumentType;
import net.arnx.jsonic.JSON;

@Entity
public class Document {
    public static class DocumentJson {
        public String name;
        public String fileName;
        public DocumentType[] types;
        public int pages;
        public int width;
        public int height;
        public int maxLevel;
        public Long[] ids; // for multiple documents
    }

    @Id
    @GeneratedValue
    public Long id;

    @Column( nullable = false )
    public Boolean processing;

    private String json;

    public String getFileName() {
        return getJson().fileName;
    }

    public int getHeight() {
        return getJson().height;
    }

    public Long[] getIds() {
        return getJson().ids;
    }
    
    private DocumentJson getJson() {
        return json != null ? JSON.decode( json , DocumentJson.class ) : new DocumentJson();
    }

    public int getMaxLevel() {
        return getJson().maxLevel;
    }

    public String getName() {
        return getJson().name;
    }

    public int getPages() {
        return getJson().pages;
    }

    public DocumentType[] getTypes() {
        return getJson().types;
    }

    public int getWidth() {
        return getJson().width;
    }

    public void setFileName( final String fileName ) {
        final DocumentJson json = getJson();
        json.fileName = fileName;
        setJson( json );
    }

    public void setHeight( final int height ) {
        final DocumentJson json = getJson();
        json.height = height;
        setJson( json );
    }

    public void setIds( final Long[] ids ) {
        final DocumentJson json = getJson();
        json.ids = ids;
        setJson( json );
    }

    private void setJson( final DocumentJson instance ) {
        json = JSON.encode( instance );
    }

    public void setMaxLevel( final int maxLevel ) {
        final DocumentJson json = getJson();
        json.maxLevel = maxLevel;
        setJson( json );
    }
    
    public void setName( final String name ) {
        final DocumentJson json = getJson();
        json.name = name;
        setJson( json );
    }

    public void setPages( final int pages ) {
        final DocumentJson json = getJson();
        json.pages = pages;
        setJson( json );
    }

    public void setTypes( final DocumentType[] types ) {
        final DocumentJson json = getJson();
        json.types = types;
        setJson( json );
    }

    public void setWidth( final int width ) {
        final DocumentJson json = getJson();
        json.width = width;
        setJson( json );
    }
}
