package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.util.ConstUtil;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class LauncherActivity extends Activity {
    private static final String URL = ConstUtil.HOST + "library.html";

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        final WebView view = new WebView( this );

        setContentView( view );

        view.getSettings().setJavaScriptEnabled( true );
        // _webView.setWebViewClient( new WebViewClient() );

        view.loadUrl( URL );
    }
}
