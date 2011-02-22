package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.core.image.CoreImageListener;
import jp.archilogic.docnext.android.core.text.CoreTextConfig;
import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.CoreTextView;
import jp.archilogic.docnext.android.core.text.TextDocDirection;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class TextViewerActivity extends Activity {
    private CoreTextView _coreTextView;
    private TextView _currentPageTextView;
    private TextView _totalPageTextView;

    private CoreTextConfig _config;

    private final CoreImageListener _coreImageListener = new CoreImageListener() {
        @Override
        public void onPageChanged( final int index ) {
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    _currentPageTextView.setText( String.valueOf( index + 1 ) );
                }
            } );
        }
    };

    private void initComonentVariable() {
        _coreTextView = ( CoreTextView ) findViewById( R.id.coreTextView );
        _currentPageTextView = ( TextView ) findViewById( R.id.CurrentPageTextView );
        _totalPageTextView = ( TextView ) findViewById( R.id.TotalPageTextView );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_NO_TITLE );

        setContentView( R.layout.text_viewer );

        initComonentVariable();

        _coreTextView
                .setSources( "　米Microsoftは21日(現地時間)、Xbox 360用3Dセンサー「Kinect」をPCで、利用するための非商用SDK(Software Development Kit)を今春公開することを明らかにした。\n\n　Microsoftは、以前よりKinectをXbox 360以外にも展開することを明らかにしていたが、すでに学術研究の場や、一部の熱心なユーザーは、PCでKinectを利用するソフトウェアを自ら開発/配布し、さまざまな応用を行なっていた。\n\n　こういった状況を受け同社は、商用バージョンよりも早いタイミングで、公式の非商用SDKを一般公開することを決めた。このSDKは、Kinectの持つシステムAPIや、各種センサーの直接制御に対するアクセスを可能にする。" );
        _coreTextView.setDirection( TextDocDirection.HORIZONTAL );
        // _coreTextView.setListener( _coreImageListener );
        _coreTextView.setConfig( _config = new CoreTextConfig() );

        findViewById( R.id.justify ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _config.useJustification = !_config.useJustification;
                _coreTextView.setConfig( _config );
            }
        } );
        findViewById( R.id.lineBreakingRule ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                switch ( _config.lineBreakingRule ) {
                case NONE:
                    _config.lineBreakingRule = LineBreakingRule.TO_NEXT;
                    break;
                case TO_NEXT:
                    _config.lineBreakingRule = LineBreakingRule.SQUEEZE;
                    break;
                case SQUEEZE:
                    _config.lineBreakingRule = LineBreakingRule.NONE;
                    break;
                default:
                    throw new RuntimeException();
                }

                _coreTextView.setConfig( _config );
            }
        } );

        _currentPageTextView.setText( String.valueOf( 1 ) );
        _totalPageTextView.setText( String.valueOf( 1 ) );
    }
}
