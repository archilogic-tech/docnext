package jp.archilogic.documentmanager.controller;

import java.io.File;
import java.io.IOException;

import jp.archilogic.documentmanager.bean.PropBean;
import jp.archilogic.documentmanager.dao.DocumentDao;
import jp.archilogic.documentmanager.entity.Document;
import jp.archilogic.documentmanager.logic.UploadProcessor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private PropBean prop;
    @Autowired
    private UploadProcessor uploadProcessor;

    @RequestMapping( "/admin/pseudo_upload" )
    public void upload( @RequestParam( "path" ) String fileName , @RequestParam( "name" ) String name )
            throws IOException {
        Document document = new Document();
        document.name = name;
        document.fileName = fileName;
        document.processing = true;
        documentDao.create( document );

        String path = "uploaded" + document.id + "." + FilenameUtils.getExtension( fileName );
        FileUtils.moveFile( new File( prop.repository + fileName ) , new File( path ) );

        uploadProcessor.proc( path , document );
    }
}
