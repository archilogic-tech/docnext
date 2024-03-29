package jp.archilogic.docnext.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import jp.archilogic.docnext.logic.PackManager;
import jp.archilogic.docnext.logic.RepositoryManager;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewerController {
    @Autowired
    private RepositoryManager repositoryManager;
    @Autowired
    private PackManager packManager;

    @RequestMapping( "/viewer/download" )
    public void download( @RequestParam( "documentId" ) final long documentId , final HttpServletResponse res )
            throws IOException {
        FileCopyUtils.copy( new FileInputStream( packManager.getPackPath( documentId ) ) , res.getOutputStream() );
    }

    @RequestMapping( "/viewer/getPage" )
    public void getPage( @RequestParam( "type" ) final String type ,
            @RequestParam( "documentId" ) final long documentId , @RequestParam( "page" ) final int page ,
            @RequestParam( "level" ) final int level , @RequestParam( "px" ) final int px ,
            @RequestParam( "py" ) final int py , final HttpServletResponse res ) {
        try {
            final InputStream in =
                    new FileInputStream( repositoryManager.getImagePath( type , documentId , page , level , px , py ) );
            final OutputStream out = res.getOutputStream();

            IOUtils.copy( in , out );

            in.close();
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
