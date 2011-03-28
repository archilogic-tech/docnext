package jp.archilogic.docnext.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Document {
    @Id
    @GeneratedValue
    public Long id;

    @Column( nullable = false )
    public String name;

    @Column( nullable = false )
    public String fileName;

    @Column( nullable = false )
    public Integer pages;

    @Column( nullable = false )
    public Integer width;

    @Column( nullable = false )
    public Integer height;

    @Column( nullable = false )
    public Integer maxLevel;

    @Column( nullable = false )
    public Boolean processing;
}
