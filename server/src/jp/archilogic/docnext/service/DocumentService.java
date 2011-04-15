package jp.archilogic.docnext.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import jp.archilogic.docnext.converter.DocumentConverter;
import jp.archilogic.docnext.converter.ListConverter;
import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.dto.DividePage;
import jp.archilogic.docnext.dto.DocumentResDto;
import jp.archilogic.docnext.dto.Frame;
import jp.archilogic.docnext.dto.TOCElem;
import jp.archilogic.docnext.entity.Document;
import jp.archilogic.docnext.exception.NotFoundException;
import jp.archilogic.docnext.logic.PackManager;
import jp.archilogic.docnext.logic.RepositoryManager;

import org.apache.commons.io.IOUtils;
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
    @Autowired
    private RepositoryManager repositoryManager;

    public List< DocumentResDto > findAll() {
        return ListConverter.toDtos( documentDao.findAlmostAll() , documentConverter );
    }

    public DocumentResDto findById( final long id ) {
        final Document document = documentDao.findById( id );

        if ( document == null ) {
            throw new NotFoundException();
        }

        return documentConverter.toDto( document );
    }

    public String getAnnotation( final long id , final int page ) {
        return packManager.readAnnotation( id , page );
    }

    public List< DividePage > getDividePage( final long id ) {
        return packManager.readDividePage( id );
    }

    public List< Frame > getFrames( final long id ) {
        return packManager.readFrames( id );
    }

    public String getImageText( final long id , final int page ) {
        return packManager.readImageText( id , page );
    }

    public String getInfo( final long id ) {
        return packManager.readInfoJson( id );
    }

    public byte[] getPage( final long id , final int page ) {
        try {
            return IOUtils.toByteArray( new FileInputStream( repositoryManager.getImagePath( "web" ,
                    id , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public byte[] getRegions( final long id , final int page ) {
        return packManager.readRegions( id , page );
    }

    public List< Integer > getSinglePageInfo( final long id ) {
        return packManager.readSinglePageInfo( id );
    }

    public String getText( final long id , final int page ) {
        return packManager.readText( id , page );
    }

    public byte[] getThumb( final long id , final int page ) {
        try {
            return IOUtils.toByteArray( new FileInputStream( repositoryManager.getImagePath(
                    "thumb" , id , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public List< TOCElem > getTOC( final long id ) {
        return packManager.readTOC( id );
    }

    public void setDividePage( final long id , final List< DividePage > dividePage ) {
        packManager.writeDividePage( id , dividePage );
    }

    public void setFrames( final long id , final List< Frame > frames ) {
        packManager.writeFrames( id , frames );
    }

    public void setSinglePageInfo( final long id , final List< Integer > singlePageInfo ) {
        packManager.writeSinglePageInfo( id , singlePageInfo );
    }

    public void setText( final long id , final int page , final String text ) {
        packManager.writeText( id , page , text.replaceAll( "\r\n" , "\n" )
                .replaceAll( "\r" , "\n" ) );
    }

    public void setTOC( final long id , final List< TOCElem > toc ) {
        packManager.writeTOC( id , toc );
    }
}
