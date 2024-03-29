package jp.archilogic.docnext.android.coreview.image;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jp.archilogic.docnext.android.Kernel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

interface Cancellable {
    void cancel();
}

interface HasPriority {
    int getPriority();
}

public class LoadBitmapTask implements Runnable , HasPriority , Cancellable {
    enum TaskStatus {
        IN_LOADING_QUEUE , IN_BINDING_QUEUE;
    }

    final int page;
    final int level;
    final int px;
    final int py;
    TaskStatus status;
    Bitmap bitmap;

    private final CoreImageState _engine;
    private final Map< Integer , List< LoadBitmapTask > > _tasks;
    private final Queue< LoadBitmapTask > _queue;

    private boolean _cancelled = false;

    LoadBitmapTask( final CoreImageState engine , final int page , final int level , final int px ,
            final int py , final Map< Integer , List< LoadBitmapTask > > tasks ,
            final Queue< LoadBitmapTask > queue ) {
        _engine = engine;
        this.page = page;
        this.level = level;
        this.px = px;
        this.py = py;
        _tasks = tasks;
        _queue = queue;

        status = TaskStatus.IN_LOADING_QUEUE;
    }

    @Override
    public void cancel() {
        _cancelled = true;
    }

    @Override
    public int getPriority() {
        return -level;
    }

    LoadBitmapTask load() {
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            String path = Kernel.getLocalProvider().getImagePath( _engine.id , page , level , px , py );
            bis =
                    new BufferedInputStream( is =
                            FileUtils.openInputStream( new File( path ) ) );

            final Options o = new Options();
            o.inPreferredConfig = Config.RGB_565;

            bitmap = BitmapFactory.decodeStream( bis , null , o );

            return this;
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( bis );
            IOUtils.closeQuietly( is );
        }
    }

    @Override
    public void run() {
        if ( _cancelled ) {
            return;
        }

        // 2 for waiting index update
        if ( page < _engine.page - 2 || page > _engine.page + 2 ) {
            return;
        }

        _tasks.get( page ).remove( this );

        status = TaskStatus.IN_BINDING_QUEUE;

        LoadBitmapTask task = load();
        _queue.add( task );
    }
}
