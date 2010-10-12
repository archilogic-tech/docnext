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
    public void download( @RequestParam( "documentId" ) long documentId , HttpServletResponse res ) throws IOException {
        FileCopyUtils.copy( new FileInputStream( packManager.getPackPath( documentId ) ) , res.getOutputStream() );
    }

    @RequestMapping( "/viewer/getPage" )
    public void getPage( @RequestParam( "type" ) String type , @RequestParam( "documentId" ) long documentId ,
            @RequestParam( "page" ) int page , @RequestParam( "level" ) int level , @RequestParam( "px" ) int px ,
            @RequestParam( "py" ) int py , HttpServletResponse res ) {
        try {
            InputStream in =
                    new FileInputStream( repositoryManager.getImagePath( type , documentId , page , level , px , py ) );
            OutputStream out = res.getOutputStream();

            IOUtils.copy( in , out );

            in.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
