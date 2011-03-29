package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.R;

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
	
	public static String EXTRA_PAGE = "page";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.table_of_contents_list);
		
		setResult(Activity.RESULT_CANCELED);

		tableOfContentsArrayAdapter = new ArrayAdapter<String>(this, R.layout.toc_title);
		ListView tableOfContentsListView = (ListView) findViewById(R.id.table_of_contents_listview);
		tableOfContentsListView.setAdapter(tableOfContentsArrayAdapter);
		tableOfContentsListView.setOnItemClickListener(mTcoClickListener);
		
		findViewById(R.id.table_of_contents_listview).setVisibility(View.VISIBLE);
		tableOfContentsArrayAdapter.add("first chapter");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    private OnItemClickListener mTcoClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_PAGE, arg2);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}
