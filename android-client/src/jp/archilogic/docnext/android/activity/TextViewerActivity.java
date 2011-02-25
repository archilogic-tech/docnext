package jp.archilogic.docnext.android.activity;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.core.OnPageChangedListener;
import jp.archilogic.docnext.android.core.text.CoreTextConfig;
import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.CoreTextInfo;
import jp.archilogic.docnext.android.core.text.CoreTextView;
import jp.archilogic.docnext.android.core.text.TextDocDirection;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.common.collect.Lists;

public class TextViewerActivity extends Activity {
    private TextView _debug;
    private CoreTextView _coreTextView;
    private TextView _currentPageTextView;
    private TextView _totalPageTextView;

    private CoreTextConfig _config;

    private final OnPageChangedListener _coreImageListener = new OnPageChangedListener() {
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

    private void bindDebug() {
        _debug.setText( String
                .format(
                        "Direction: %s, FontSize: %d, LineSpace: %.1f, hPad: %.1f, vPad: %.1f, Justification: %s, LineBreaking: %s, PageSpace: %.1f, Background: %X, DefaultColor: %X,  RubyFactor %d" , //
                        _config.direction , //
                        _config.fontSize , //
                        _config.lineSpace , //
                        _config.horizontalPadding , //
                        _config.verticalPadding , //
                        _config.useJustification ? "ON" : "OFF" , //
                        _config.lineBreakingRule == LineBreakingRule.NONE ? //
                                "None" : _config.lineBreakingRule == LineBreakingRule.TO_NEXT ? "Wrap to next"
                                        : "Squeeze" , //
                        _config.pageSpace , //
                        _config.backgroundColor , //
                        _config.defaultTextColor , //
                        _config.rubyFontSizeFactor ) );
    }

    private void initComonentVariable() {
        _debug = ( TextView ) findViewById( R.id.debug );
        _coreTextView = ( CoreTextView ) findViewById( R.id.coreTextView );
        _currentPageTextView = ( TextView ) findViewById( R.id.CurrentPageTextView );
        _totalPageTextView = ( TextView ) findViewById( R.id.TotalPageTextView );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.text_viewer );

        initComonentVariable();

        final String sample =
                "電子書籍フォーマットの国際標準仕様を策定している、IDPF（International Digital Publishing Forum、国際電子出版フォーラム）は15日、現在策定中の電子書籍フォーマット、「EPUB 3」の境界面パブリックドラフトを公開した、と発表しました。\n\n"
                        + "EPUB 3 Specification Public Draft Released | International Digital Publishing Forum\n\n"
                        + "EPUB 3 は、HTML5 と CSS3 など現在 W3C で策定中の最新の Web 標準をベースにした、オープンな電子書籍フォーマット。EPUB はアップルの iPad や Google Books、ソニーの Reader などで採用されており、PC でも EPUB リーダーをインストールすることで表示可能で、電子書籍の有力な国際標準フォーマット、と考えられています。\n\n"
                        + "EPUB 3 は今年5月に完成予定\n\n"
                        + "EPUB 3 の仕様には、縦書きやルビ、圏点（傍点）、禁則といった日本語の書籍に不可欠だった要素が含まれているため、EPUB 3 の登場は、国内での電子書籍の普及を促進することが期待されています。と同時に、どんなデバイスやソフトウェアであっても EPUB 3 に対応していれば、日本の電子書籍に対応できるようになるため、マンガや小説など、あらゆる日本の出版物が、国際市場へ踏み出すためのチャンスとしても期待されています。\n\n"
                        + "EPUB 3 に、そうした日本語を含む国際化仕様を組み込んでいるのが、EPUB のサブグループ「Enhanced Global Language Support」（EGLS）で、そのコーディネータは、日本人の村田真氏が努めています。村田氏は少し前に、EPUB 3 そのもののエディタにも任命されたようで、公開された EPUB 3 のパブリックドラフトには Editors の欄に村田氏も名前を連ねています。\n\n"
                        + "Webkit による縦書きやルビといった日本語組み版機能の実装も進んでいる。Webkit をベースにした EPUB リーダーも登場することだろう\n\n"
                        + "現在、EPUB 3 の縦書きやルビ、圏点といった、仕様の参照の基となる W3C の CSS3 の策定も含め、関係者は最後の詰めの作業に入っており、仕様に合わせて Webkit による実装も進んでいます。EPUB 3 の パブリックドラフトは、当初12月に公開される予定でしたので、やや予定より遅れているように見えますが、今のところ今年5月に完成するという予定に変更はなく、完成が待たれています。あぁいぃうぅえぇおぉつっやゃゆゅよょ";
        _coreTextView.setSources( Lists.newArrayList( new CoreTextInfo() {
            {
                text = sample;

                rubys = Lists.newArrayList();
                rubys.add( new Ruby( "でんししょせき" , 0 , 4 ) );
                rubys.add( new Ruby( "こくさいひょうじゅんしよう" , 11 , 6 ) );
                rubys.add( new Ruby( "さくてい" , 18 , 2 ) );
                rubys.add( new Ruby( "アイディーピーエフ" , 25 , 4 ) );
                rubys.add( new Ruby( "こくさいでんししゅっぱん" , 69 , 6 ) );
                rubys.add( new Ruby( "げんざい" , 86 , 2 ) );
                rubys.add( new Ruby( "イーブック" , 92 , 4 ) );
                rubys.add( new Ruby( "インターフェース" , 112 , 3 ) );

                dots = Lists.newArrayList();
                dots.add( new Dot( 4 , 6 ) );

                tcys = Lists.newArrayList();
                tcys.add( new TCY( 82 , 2 ) );
            }
        } , new CoreTextInfo() {
            {
                text = "**2** " + sample;
                rubys = Lists.newArrayList();
                dots = Lists.newArrayList();
                tcys = Lists.newArrayList();
            }
        } , new CoreTextInfo() {
            {
                text = "**3** " + sample;
                rubys = Lists.newArrayList();
                dots = Lists.newArrayList();
                tcys = Lists.newArrayList();
            }
        } , new CoreTextInfo() {
            {
                text = "**4** " + sample;
                rubys = Lists.newArrayList();
                dots = Lists.newArrayList();
                tcys = Lists.newArrayList();
            }
        } , new CoreTextInfo() {
            {
                text = "**5** " + sample;
                rubys = Lists.newArrayList();
                dots = Lists.newArrayList();
                tcys = Lists.newArrayList();
            }
        } , new CoreTextInfo() {
            {
                text = "**6** " + sample;
                rubys = Lists.newArrayList();
                dots = Lists.newArrayList();
                tcys = Lists.newArrayList();
            }
        } , new CoreTextInfo() {
            {
                text = "**7** " + sample;
                rubys = Lists.newArrayList();
                dots = Lists.newArrayList();
                tcys = Lists.newArrayList();
            }
        } ) );
        _coreTextView.setListener( _coreImageListener );
        _coreTextView.setConfig( _config = new CoreTextConfig() );

        bindDebug();

        findViewById( R.id.justify ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _config.useJustification = !_config.useJustification;
                _coreTextView.setConfig( _config );

                bindDebug();
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

                bindDebug();
            }
        } );
        findViewById( R.id.bigger ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _config.fontSize++;
                _coreTextView.setConfig( _config );

                bindDebug();
            }
        } );
        findViewById( R.id.smaller ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _config.fontSize--;
                _coreTextView.setConfig( _config );

                bindDebug();
            }
        } );
        findViewById( R.id.reverse ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _config.backgroundColor ^= _config.defaultTextColor;
                _config.defaultTextColor ^= _config.backgroundColor;
                _config.backgroundColor ^= _config.defaultTextColor;

                _coreTextView.setConfig( _config );

                bindDebug();
            }
        } );
        findViewById( R.id.direction ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                _config.direction =
                        _config.direction == TextDocDirection.HORIZONTAL ? TextDocDirection.VERTICAL
                                : TextDocDirection.HORIZONTAL;

                _coreTextView.setConfig( _config );

                bindDebug();
            }
        } );

        _currentPageTextView.setText( String.valueOf( 1 ) );
        _totalPageTextView.setText( String.valueOf( 1 ) );
    }
}
