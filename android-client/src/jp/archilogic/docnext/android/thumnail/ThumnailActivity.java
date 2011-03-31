package jp.archilogic.docnext.android.thumnail;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class ThumnailActivity extends Activity {

    public static String EXTRA_PAGE = "page";

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
        
        GridView gridview = ( GridView ) findViewById( R.id.thumnail );
        ThumnailImageAdapter adapter = new ThumnailImageAdapter( this, ids[0] );
        gridview.setAdapter( adapter );
        
        gridview.setOnItemClickListener( new OnItemClickListener() {
            public void onItemClick( AdapterView< ? > parent, View v, int position, long id ) {
                Intent intent = new Intent();
                intent.putExtra( CoreViewActivity.EXTRA_PAGE, position + 1 );

                setResult( Activity.RESULT_OK, intent );
                finish();

            }
        });
    }
}
