package jp.archilogic.docnext.service;

import java.util.List;

import jp.archilogic.docnext.converter.DocumentConverter;
import jp.archilogic.docnext.converter.ListConverter;
import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.dto.DocumentResDto;
import jp.archilogic.docnext.dto.TOCElem;
import jp.archilogic.docnext.entity.Document;
import jp.archilogic.docnext.exception.NotFoundException;
import jp.archilogic.docnext.logic.PackManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Component;

@Component
@RemotingDestination
public class DocumentService {
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private DocumentConverter documentConverter;
    @Autowired
    private PackManager packManager;

    public List< DocumentResDto > findAll() {
        return ListConverter.toDtos( documentDao.findAlmostAll() , documentConverter );
    }

    public DocumentResDto findById( long id ) {
        Document document = documentDao.findById( id );

        if ( document == null ) {
            throw new NotFoundException();
        }

        return documentConverter.toDto( document );
    }

    public String getPublisher( long id ) {
        return packManager.readPublisher( id );
    }

    public List< Integer > getSinglePageInfo( long id ) {
        return packManager.readSinglePageInfo( id );
    }

    public String getText( long id , int page ) {
        return packManager.readText( id , page );
    }

    public String getTitle( long id ) {
        return packManager.readTitle( id );
    }

    public List< TOCElem > getTOC( long id ) {
        return packManager.readTOC( id );
    }

    public void repack( long id ) {
        packManager.repack( id );
    }

    public void setPublisher( long id , String publisher ) {
        packManager.writePublisher( id , publisher );
    }

    public void setSinglePageInfo( long id , List< Integer > singlePageInfo ) {
        packManager.writeSinglePageInfo( id , singlePageInfo );
    }

    public void setText( long id , int page , String text ) {
        packManager.writeText( id , page , text.replaceAll( "\r\n" , "\n" ).replaceAll( "\r" , "\n" ) );
    }

    public void setTitle( long id , String title ) {
        packManager.writeTitle( id , title );
    }

    public void setTOC( long id , List< TOCElem > toc ) {
        packManager.writeTOC( id , toc );
    }
}