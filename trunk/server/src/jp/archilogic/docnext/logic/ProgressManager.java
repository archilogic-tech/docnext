package jp.archilogic.docnext.logic;

import java.util.Map;

import jp.archilogic.docnext.dao.DocumentDao;
import jp.archilogic.docnext.entity.Document;
import net.arnx.jsonic.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class ProgressManager {
    public class Progress {
        public Step step;
        public int createdThumbnail;
        public int totalThumbnail;

        public Progress() {
        }

        public Progress( Step step ) {
            this.step = step;
        }
    }

    public class ProgressJSON {
        public String step;
        public int createdThumbnail;
        public int totalThumbnail;

        public ProgressJSON( Progress progress ) {
            step = progress.step.toString();
            createdThumbnail = progress.createdThumbnail;
            totalThumbnail = progress.totalThumbnail;
        }
    }

    public enum Step {
        WAITING_EXEC , INITIALIZING , CREATING_THUMBNAIL , COMPLETED , FAILED;
    }

    @Autowired
    private DocumentDao documentDao;

    private final Map< Long , Progress > _data = Maps.newHashMap();

    public void clearCompleted( long id ) {
        _data.remove( id );
    }

    public String getProgressJSON( long id ) {
        Progress progress = _data.get( id );

        if ( progress == null ) {
            Document document = documentDao.findById( id );

            progress = new Progress( document != null && !document.processing ? Step.COMPLETED : Step.FAILED );
        }

        return JSON.encode( new ProgressJSON( progress ) );
    }

    public void setCreatedThumbnail( long id , int created ) {
        Progress progress = _data.get( id );

        if ( progress == null ) {
            _data.put( id , progress = new Progress() );
        }

        progress.createdThumbnail = created;
    }

    public void setStep( long id , Step step ) {
        Progress progress = _data.get( id );

        if ( progress == null ) {
            _data.put( id , progress = new Progress() );
        }

        progress.step = step;
    }

    public void setTotalThumbnail( long id , int total ) {
        Progress progress = _data.get( id );

        if ( progress == null ) {
            _data.put( id , progress = new Progress() );
        }

        progress.totalThumbnail = total;
    }
}
