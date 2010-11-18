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
    public enum ErrorType {
        UNKNOWN , ENCRYPTED;
    }

    public class Progress {
        public Step step;
        public int createdThumbnail;
        public int totalThumbnail;
        public ErrorType error;

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
        public String error;

        public ProgressJSON( Progress progress ) {
            step = progress.step.toString();
            createdThumbnail = progress.createdThumbnail;
            totalThumbnail = progress.totalThumbnail;
            error = progress.error != null ? progress.error.toString() : "no error";
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

        // clear Progress which is set by setError
        if ( progress != null && progress.step == Step.FAILED ) {
            _data.remove( id );
        }

        if ( progress == null ) {
            Document document = documentDao.findById( id );

            if ( document != null && !document.processing ) {
                progress = new Progress( Step.COMPLETED );
            } else {
                progress = new Progress( Step.FAILED );
                progress.error = ErrorType.UNKNOWN;
            }
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

    public void setError( long id , ErrorType error ) {
        setStep( id , Step.FAILED );

        _data.get( id ).error = error;
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
