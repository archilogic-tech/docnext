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
    public String getProgress( @RequestParam( "id" ) final long id ) {
        return progressManager.getProgressJSON( id );
    }

    @RequestMapping( "/admin/upload" )
    @ResponseBody
    public String upload( @RequestParam( "name" ) final String name , @RequestParam( "file" ) final MultipartFile file )
            throws IOException {
        final Document doc = new Document();
        doc.name = name;
        doc.fileName = file.getOriginalFilename();
        doc.processing = true;
        doc.nLevel = -1;
        documentDao.create( doc );

        final String path = "uploaded" + doc.id + "." + FilenameUtils.getExtension( file.getOriginalFilename() );
        final String uploadPath = prop.tmp + File.separator + doc.id + File.separator + path;
        FileUtils.writeByteArrayToFile( new File( uploadPath ) , file.getBytes() );

        uploadProcessor.proc( uploadPath , doc );

        progressManager.setStep( doc.id , Step.WAITING_EXEC );

        return String.valueOf( doc.id );
    }
}
