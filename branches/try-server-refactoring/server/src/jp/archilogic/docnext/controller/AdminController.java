package jp.archilogic.docnext.controller;

import java.io.File;
import java.io.IOException;

import jp.archilogic.docnext.bean.PropBean;
import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.entity.Document;
import jp.archilogic.docnext.logic.ProgressManager;
import jp.archilogic.docnext.logic.ProgressManager.Step;
import jp.archilogic.docnext.logic.UploadProcessor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private PropBean prop;
    @Autowired
    private UploadProcessor uploadProcessor;
    @Autowired
    private ProgressManager progressManager;

    @RequestMapping( "/admin/getProgress" )
    @ResponseBody
    public String getProgress( @RequestParam( "id" ) long id ) {
        return progressManager.getProgressJSON( id );
    }

    private String getTempPath() {
        String path = prop.tmp;

        if ( path == null || path.isEmpty() ) {
            path = System.getProperty( "java.io.tmpdir" );
        }

        return path;
    }

    @RequestMapping( "/admin/upload" )
    @ResponseBody
    public String upload( @RequestParam( "name" ) String name , @RequestParam( "file" ) MultipartFile file )
            throws IOException {
        Document doc = new Document();
        doc.name = name;
        doc.fileName = file.getOriginalFilename();
        doc.processing = true;
        documentDao.create( doc );

        String path = "uploaded" + doc.id + "." + FilenameUtils.getExtension( file.getOriginalFilename() );
        String uploadPath = getTempPath() + File.separator + doc.id + File.separator + path;
        FileUtils.writeByteArrayToFile( new File( uploadPath ) , file.getBytes() );

        uploadProcessor.proc( uploadPath , doc );

        progressManager.setStep( doc.id , Step.WAITING_EXEC );

        return String.valueOf( doc.id );
    }


    public long uploadFile(String name , String filename, byte[] file ) throws IOException {
        Document document = new Document();
        document.name = name;
        document.fileName = filename;
        document.processing = true;
        documentDao.create( document );

        String path = "uploaded" + document.id + "." + FilenameUtils.getExtension( filename );
        String uploadPath = getTempPath() + File.separator + document.id + File.separator + path;
        FileUtils.writeByteArrayToFile( new File( uploadPath ) , file);

        uploadProcessor.proc( uploadPath , document );
        return document.id;
    }

}
