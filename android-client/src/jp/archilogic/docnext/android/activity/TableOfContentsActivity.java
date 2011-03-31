package jp.archilogic.docnext.android.activity;

import java.util.ArrayList;
import java.util.List;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.info.TOCElement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TableOfContentsActivity extends Activity {
    private ArrayAdapter<String> tableOfContentsArrayAdapter;
    private ArrayList<Integer> pageList;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        final long[] ids = getIntent().getLongArrayExtra( CoreViewActivity.EXTRA_IDS );
        if ( ids == null || ids.length == 0 ) {
            throw new RuntimeException();
        }

        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.table_of_contents_list );

        setResult( Activity.RESULT_CANCELED );

        tableOfContentsArrayAdapter = new ArrayAdapter<String>( this,
                R.layout.toc_title );
        ListView tableOfContentsListView = (ListView) findViewById( R.id.table_of_contents_listview );
        tableOfContentsListView.setAdapter( tableOfContentsArrayAdapter );
        tableOfContentsListView.setOnItemClickListener( mTcoClickListener );

        findViewById( R.id.table_of_contents_listview ).setVisibility(
                View.VISIBLE );

        pageList = new ArrayList<Integer>();
        List<TOCElement> tableOfContents = Kernel.getLocalProvider()
                .getTableOfContentsInfo( ids[0] );

        for (TOCElement element : tableOfContents) {
            pageList.add( element.page );
            tableOfContentsArrayAdapter.add( element.text );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private OnItemClickListener mTcoClickListener = new OnItemClickListener() {
        public void onItemClick( AdapterView<?> av, View v, int arg2, long arg3 ) {
            Intent intent = new Intent();
            intent.putExtra( CoreViewActivity.EXTRA_PAGE, pageList.get( arg2 ) );

            setResult( Activity.RESULT_OK, intent );
            finish();
        }
    };
}
