package jp.archilogic.docnext.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropBean {
    @Value( "${path.repository}" )
    public String repository;

    @Value( "${path.host}" )
    public String host;

    @Value( "${path.pdfToPpm}" )
    public String pdfToPpm;

    @Value( "${path.convert}" )
    public String convert;

    @Value( "${path.identify}" )
    public String identify;

    @Value( "${path.pdfinfo}" )
    public String pdfInfo;

    @Value( "${path.tmp}" )
    public String tmp;

    @Value( "${image.ios}" )
    public boolean forIOS;

    @Value( "${image.web}" )
    public boolean forWeb;

    @Value( "${image.texture}" )
    public boolean forTexture;
}
