package jp.archilogic.docnext.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.dto.DocInfo;
import jp.archilogic.docnext.dto.ImageInfo;
import jp.archilogic.docnext.entity.Document;
import jp.archilogic.docnext.logic.PackManager;
import jp.archilogic.docnext.logic.RepositoryManager;
import jp.archilogic.docnext.logic.ThumbnailCreator;
import net.arnx.jsonic.JSON;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ViewerController {
    @Autowired
    private RepositoryManager repositoryManager;
    @Autowired
    private PackManager packManager;
    @Autowired
    private DocumentDao documentDao;

    @RequestMapping( "/viewer/download" )
    public void download( @RequestParam( "documentId" ) final long documentId , final HttpServletResponse res )
            throws IOException {
        FileCopyUtils.copy( new FileInputStream( packManager.getPackPath( documentId ) ) , res.getOutputStream() );
    }

    @RequestMapping( "/viewer/getDocInfo" )
    @ResponseBody
    public String getDocInfo( @RequestParam( "id" ) final long id ) {
        final Document doc = documentDao.findById( id );

        return JSON.encode( new DocInfo( id , doc.getTypes() , doc.getPages() ) );
    }

    @RequestMapping( "/viewer/getImageInfo" )
    @ResponseBody
    public String getImageInfo( @RequestParam( "id" ) final long id , @RequestParam( "shortSide" ) final int shortSide ) {
        final int LIMIT = 3;

        final Document doc = documentDao.findById( id );

        final int minLevel = getMinLevel( shortSide );

        final int width = ( int ) ( ThumbnailCreator.TEXTURE_SIZE * Math.pow( 2 , minLevel ) );
        final int height = doc.getHeight() * width / doc.getWidth();
        final int nLevel = Math.min( doc.getMaxLevel() - minLevel + 1 , LIMIT );

        return JSON.encode( new ImageInfo( width , height , nLevel ) );
    }

    private int getMinLevel( final int shortSide ) {
        return ( int ) Math.ceil( Math.log( 1.0 * shortSide / ThumbnailCreator.TEXTURE_SIZE ) / Math.log( 2 ) );
    }

    @RequestMapping( "/viewer/getPage" )
    public void getPage( @RequestParam( "type" ) final String type ,
            @RequestParam( "documentId" ) final long documentId , @RequestParam( "page" ) final int page ,
            @RequestParam( "level" ) final int level , @RequestParam( "px" ) final int px ,
            @RequestParam( "py" ) final int py , final HttpServletResponse res ) {
        try {
            final InputStream in =
                    FileUtils.openInputStream( new File( repositoryManager.getImagePath( type , documentId , page ,
                            level , px , py ) ) );
            final OutputStream out = res.getOutputStream();

            IOUtils.copy( in , out );

            IOUtils.closeQuietly( in );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
    
    @RequestMapping( "/viewer/getTOC" )
    @ResponseBody
    public String getTOC( @RequestParam( "id" ) final long id )
    throws IOException {
    	return JSON.encode( packManager.readTOC( id ) );
    }

    @RequestMapping( "/viewer/getText" )
    @ResponseBody
    public String getText( @RequestParam( "id" ) final long id , @RequestParam( "page" ) final int page ) {
        try {
            return FileUtils.readFileToString( new File( repositoryManager.getTextPath( id , page ) ) );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @RequestMapping( "/viewer/smartGetPage" )
    public void smartGetPage( @RequestParam( "id" ) final long id , @RequestParam( "page" ) final int page ,
            @RequestParam( "level" ) final int level , @RequestParam( "px" ) final int px ,
            @RequestParam( "py" ) final int py , @RequestParam( "shortSide" ) final int shortSide ,
            final HttpServletResponse res ) {
        try {
            final InputStream in =
                    FileUtils.openInputStream( new File( repositoryManager.getImagePath( id , page ,
                            getMinLevel( shortSide ) + level , px , py ) ) );
            final OutputStream out = res.getOutputStream();

            IOUtils.copy( in , out );

            IOUtils.closeQuietly( in );
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
