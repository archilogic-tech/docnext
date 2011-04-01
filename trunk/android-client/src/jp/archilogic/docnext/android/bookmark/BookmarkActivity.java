package jp.archilogic.docnext.android.bookmark;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.provider.local.LocalPathManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class BookmarkActivity extends Activity {
    long id = -1;
    //public static String EXTRA_PAGE = "page";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.thumnail );
        setResult( Activity.RESULT_CANCELED );
        
        final long[] ids = getIntent().getLongArrayExtra( CoreViewActivity.EXTRA_IDS );
        if ( ids == null || ids.length == 0 ) {
            throw new RuntimeException();
        }
        id = ids[0];

        LocalPathManager localPathManager = new LocalPathManager();
        localPathManager.ensureBookmark( id );
        
        int currentPage = getIntent().getIntExtra( CoreViewActivity.EXTRA_CURRENT_PAGE, -1 );
        TreeSet< Integer > bookmarkSet = new TreeSet< Integer >(); 
        bookmarkSet.addAll( Kernel.getLocalProvider().getBookmarkInfo( id ) );
        bookmarkSet.add( currentPage );
        List< Integer > bookmarkList = new ArrayList<Integer>();
        bookmarkList.addAll( bookmarkSet );
        Kernel.getLocalProvider().setBookmarkInfo( id, bookmarkList );
        
        GridView gridview = ( GridView ) findViewById( R.id.thumnail );
        BookmarkImageAdapter adapter = new BookmarkImageAdapter( this, ids[0] );
        gridview.setAdapter( adapter );
        
        gridview.setOnItemClickListener( new OnItemClickListener() {
            public void onItemClick( AdapterView< ? > parent, View v, int position, long aId ) {
                Intent intent = new Intent();
                int page = Kernel.getLocalProvider().getBookmarkInfo( id ).get( position );
                intent.putExtra( CoreViewActivity.EXTRA_PAGE, page );

                setResult( Activity.RESULT_OK, intent );
                finish();

            }
        });
    }
}
