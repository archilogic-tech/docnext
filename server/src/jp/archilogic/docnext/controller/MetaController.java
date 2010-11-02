package jp.archilogic.docnext.controller;

import java.io.IOException;

import jp.archilogic.docnext.logic.PackManager;
import net.arnx.jsonic.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MetaController {
    class MetaInfo {
        public int pages;
        public String title;
    }

    @Autowired
    private PackManager packManager;

    @RequestMapping( "/meta/info" )
    @ResponseBody
    public String info( @RequestParam( "documentId" ) long docId ) throws IOException {
        MetaInfo ret = new MetaInfo();
        ret.pages = packManager.readPages( docId );
        ret.title = packManager.readTitle( docId );

        return JSON.encode( ret );
    }
}
