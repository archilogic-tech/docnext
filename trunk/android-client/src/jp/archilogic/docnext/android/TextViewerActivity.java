package jp.archilogic.docnext.android;

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
                text =
                        "講談社電子文庫\n"
                                + "\n"
                                + "\n"
                                + "金田一少年の事件簿\n"
                                + "幽霊客船殺人事件\n"
                                + "\n"
                                + "\n"
                                + "天樹征丸　著　\n"
                                + "\n"
                                + "\n"
                                + "目　次\n"
                                + "\n"
                                + "\n"
                                + "プロローグ\n"
                                + "第一章　竜王丸航海日誌\n"
                                + "第二章　出　航\n"
                                + "第三章　幽霊船マリー・セレスト号\n"
                                + "第四章　悪夢の夜\n"
                                + "第五章　身代わりの死S\n"
                                + "『読者への挑戦状』\n"
                                + "第六章　真　相\n"
                                + "エピローグ\n"
                                + "\n"
                                + "\n"
                                + "\n"
                                + "シンボルマークデザイン／安野光雅\n"
                                + "\n"
                                + "\n"
                                + "●主な登場人物\n"
                                + "\n"
                                + "\n"
                                + "金田一一………名探偵金田一耕助の孫。高校二年生。\n"
                                + "七瀬美雪………一の幼なじみにしてクラスメート。\n"
                                + "剣持警部………警視庁捜査一課の警部で、一を高く評価している。\n"
                                + "剣持和枝………剣持警部の妻。\n"
                                + "鷹守郷三………船長。\n"
                                + "若王子幹彦……一等航海士。\n"
                                + "水崎丈次………二等航海士。\n"
                                + "加納達也………三等航海士。\n"
                                + "大槻健太郎……機関長。\n"
                                + "香取洋子………乗務員。\n"
                                + "赤井義和………怪奇写真家。\n"
                                + "ナカムラ・イチロウ……謎の乗船客。\n"
                                + "大沢貴志………乗船客。\n"
                                + "美里朱美………乗船客。\n"
                                + "飯島優…………乗船客。\n"
                                + "\n"
                                + "幽霊客船殺人事件\n"
                                + "\n"
                                + "\n"
                                + "プロローグ\n"
                                + "\n"
                                + "\n"
                                + "　それは、確かに帆船だった。\n"
                                + "　しかし、深い霧のはざまから、ぼうっと浮かびあがるように姿を現したその船は、蜃気楼か、さもなくば伝説に聞く幽霊船のように船員たちには見えた。\n"
                                + "　一八七二年十二月のことである。\n"
                                + "　最初の寄港地ジブラルタルに向かって、大西洋上を航行していた帆船デイ・グラティア号は、深い霧の中で奇妙な様子の船に出くわした。\n"
                                + "　その帆船は、帆の大部分がボロ布のようにたれさがっていて、酒に酔ったようにふらふらと左右に向きを変えながら航行を続けていたのである。\n"
                                + "　不審に思ったグラティア号のモアハウス船長は、操舵手に命じた。\n"
                                + "「速度をあげろ。あの船に追いつくんだ」\n"
                                + "　グラティア号が追いはじめると、奇妙な帆船はそれを待っていたかのように速度を落とした。\n"
                                + "　グラティア号が信号弾を打ち上げても返答はない。近づいて双眼鏡をのぞいたが、甲板に船員の気配はなかった。いや、それどころか操舵輪を操る者さえいないように見えた。\n"
                                + "「船長、なにか異変でもあって救助を待っているのかもしれません。わたしが行ってみます」\n"
                                + "　一等航海士のオリバー・デボーが、そう言って探索を志願した。\n"
                                + "「よし、ボートを出そう」\n"
                                + "　船長の命令でオリバーは二人の船員をともない、もやの這う寒々とした海にボートを漕ぎだした。\n"
                                + "　三人は船の反対側にボートを回し、船腹に書かれている船名を見上げた。\n"
                                + "\n"
                                + "『マリー・セレスト号』\n"
                                + "\n"
                                + "　波と霧でしっとりと濡れた木製の船体に、英語でそう書かれていた。\n"
                                + "「……乗り込むぞ」\n"
                                + "　オリバー・デボーは、気味悪がって反対する船員一人をボートに残し、もう一名を連れてセレスト号の甲板によじのぼった。\n"
                                + "「こ、これはいったい……？」\n"
                                + "　二人は、船上のあまりにも奇怪な様子に、言葉をなくした。\n"
                                + "　操舵輪は、操る者もなく、壊れた風車のように気ままに回り続けている。たれさがった帆がバタバタと飛び立つ鳥のような音をたてているのに、それを整えようとする甲板員の姿さえなかった。\n"
                                + "　船内を探索した二人は、さらに異様な光景に出くわした。\n"
                                + "　キッチンも食堂も乗組員の部屋も、すべてが整然とし、騒動のあとなどどこにもない。にもかかわらず、誰ひとり船員の姿が見当たらないのだ。\n"
                                + "　船員たちの衣類は、きれいに洗濯してたたまれていた。キッチンには作りかけの朝食がそのまま残っている。食卓の上に、パンや乾し肉をのせた皿が並び、ナプキンや食器まで整えられていた。\n"
                                + "　今にも、そこのドアを開けて日に焼けた船員たちがにぎやかに現れ、待ちかねたように朝食をとりはじめそうなほど、それはありふれた日常の光景だった。\n"
                                + "「ひ、引き返しましょう、デボー航海士」\n"
                                + "　迷信深い船員が、胸で十字を切りながらそう言った。\n"
                                + "「まだ船長室が残ってる。誰かいるかもしれない。いや、きっといるはずだ。いなければならないのだ」\n"
                                + "　恐怖を打ち消そうとする気持ちが、デボーを勇敢にした。\n"
                                + "　熱病に冒されたように震える船員をむりやり従わせて、デボーは船長室のドアを開けさせた。\n"
                                + "「……！」\n"
                                + "　そこに、船長の姿はなかった。\n"
                                + "　ただ、朝食のために用意されたであろう食器、その上に残されたパンとゆで卵だけが、主人のいなくなった食卓に、なにかのメッセージのように並んでいた。\n"
                                + "「きっと、嵐かなにかで混乱におちいって、全員が脱出したんですよ。そうにきまっている！」\n"
                                + "　むきになって主張する船員に、デボーは食卓の上のカップを指して言った。\n"
                                + "「いや、ちがう。だったらこのカップに残っていたらしいコーヒーは、あたりにこぼれているはずだし、そこの薬ビンにしても倒れていなくてはなるまい。嵐なんぞなかった。この船から姿を消す直前まで、船長はここで優雅に朝食をとっていたのだ」\n"
                                + "　ふとベッド脇の机に目をやったデボーは、その上に本のようなものが開いたままになっていることに気づいた。\n"
                                + "　それは船長が書き残した、マリー・セレスト号の航海日誌だった。\n"
                                + "　日誌には、さしたる異変は書かれておらず、航海が平穏に続けられていたことを物語っている。\n"
                                + "　そして、航海日誌の記述は十日前の、十一月二十五日の朝で終わっていた。\n"
                                + "「信じられない……」\n"
                                + "　この無人の船は十日間ものあいだ、優れた船長が舵をとっていたかのように、きちんと航路を守って航海を続けてきたのだ。\n"
                                + "「幽霊が舵をとっていたとでもいうのか？」\n"
                                + "　オリバーは、背筋が凍りつく思いがした。\n"
                                + "\n"
                                + "　腰が抜けたように座り込んで、神の名をつぶやき続ける船員をひきずるようにして、オリバーは甲板に上がった。\n"
                                + "　すぐ近くにデイ・グラティア号の姿をみとめた時は、さしものオリバーもほっと胸をなでおろした。\n"
                                + "　ボートで待つ船員に合図を送り、船縁の手すりを乗り越えようとした時、彼はふと甲板を振り返った。人の気配を感じたような気がしたのだ。\n"
                                + "　しかし、そこに“目に見える人間の姿”はなかった。\n"
                                + "　深い霧のたちこめる“無人”の甲板を見渡して、オリバーはつぶやいた。\n"
                                + "\n"
                                + "「幽霊船長……」\n"
                                + "\n"
                                + "\n"
                                + "第一章　竜王丸航海日誌\n"
                                + "\n"
                                + "１\n"
                                + "\n"
                                + "「オッサン、いいかげんにそれに決めちゃえよ」\n"
                                + "　さめたコーヒーを、わざとらしく音をたててすすりながら、金田一一は言った。\n"
                                + "「しかしなあ、金田一。朝食しかついてないのに、この値段はちと高いぞ。せめてもう一万円くらい……」\n"
                                + "“オッサン”は、そう言って薄くなりはじめた頭をかいた。\n"
                                + "「だったらこれはどうかしら。ほら、三食ついて十二万円なら、リーズナブルですよ」\n"
                                + "　七瀬美雪が、ピンクのラインマーカーでパンフレットに印をつけながら言った。\n"
                                + "「じゅ、十二万!?　いくら三食昼寝つきでも高すぎだ」\n"
                                + "「昼寝はついてねーよ、オッサン」\n"
                                + "「……コーヒー、おつぎしましょうか？」\n"
                                + "　コーヒーのおかわりをもってきたウエイトレスが、笑いをこらえながら言った。\n"
                                + "　昼食時のファミリーレストランで、四十代半ばの中年男と高校生らしき男女の三人が、夏休みの計画を練る友達同士のように、旅行パンフレットを広げて騒いでいるさまは、店員たちや昼休みのサラリーマンには、どう映っているのだろう。\n"
                                + "　息子と娘に夏休みの旅行をねだられているお父さん？　それとも、学校の先生が学級委員の生徒と一緒に、クラス旅行の計画をたてている？\n"
                                + "　いや、じつは“オッサン”呼ばわりされているこの中年男、れっきとした警視庁捜査一課の警部どのなのである。\n"
                                + "　本庁“殺人課”のたたき上げ警部である剣持勇が、長髪を後ろで束ねたいかにも“今どきの若者”風の少年、金田一一にオッサン呼ばわりされるようになったきっかけは、『オペラ座館殺人事件』として新聞紙上を騒がせた奇怪な連続殺人事件であった。\n"
                                + "　孤島の古いホテルで起きた、この異常きわまりない殺人事件を解決に導いたのが、ほかならぬ金田一一だったのである。一は自分の通う不動高校の演劇部の合宿に、部員である幼なじみの七瀬美雪の誘いで参加することになり、事件に巻き込まれたのであった。\n"
                                + "　非番の休暇中この事件に居合わせた剣持警部は、一の驚異的な推理力に一目置くことになり、以来、かの名探偵金田一耕助の孫だというこの少年と、公私ともどもの腐れ縁を続けているのである。\n"
                                + "「よーし、じゃ、これに決定な。九万八千円沖縄ムーンビーチ四泊五日！」\n"
                                + "　一は、めんどうくさそうにパンフレットの山をかたづけ、さっさと席を立とうとしている。\n"
                                + "「――そろそろ学校にもどんないと、午後の授業はじまっちまうからさ」\n"
                                + "と言いつつ一は、午後はサボリを決め込む気でいた。今日は、ちょっと大事な用事があるのだ。この夏休みが思い出に残る夏になるか否か、この用事にかかっている――はずだった。\n"
                                + "「おいまてよ、金田一」\n"
                                + "と、剣持。\n"
                                + "「――ボーナスは車とマンションのローンでほとんど消えちまうんだ。ガキの受験のこともあるし、あんまりむだづかいするわけには……」\n"
                                + "と、剣持。\n"
                                + "「あら、剣持警部、むだづかいはひどいじゃないですか。十五回目の結婚記念日なんでしょ？　パーッと奮発しちゃいましょうよ。きっと奥さん喜ぶと思うなあ。あ、これなんか素敵ね。沖縄石垣島リゾートホテルステイ七日間、十六万八千円。ほら、プライベートビーチでマリンスポーツやりほうだいですって。きゃーっ、いいなあ、あたしもいつか行きたーい」\n"
                                + "　美雪は、自分のことのようにはしゃぎながら勝手にパンフレットにラインマーカーで印をつけている。\n"
                                + "「だーっ！　いいかげんにしろ」\n"
                                + "　剣持が切れた。\n"
                                + "「――なんのためにお前らに、パンフレット集めを頼んだと思ってるんだ。おれは忙しくてできそうもないから、お前らに安くて豪華なツアーを探してくれと、勤務中に抜け出してこんなとこで昼飯までおごって……」\n"
                                + "「わーってる、わーってる。ちゃんと安いツアーも探してあるってば」\n"
                                + "　一は、紙袋から印刷の悪い薄っぺらなパンフレットを出してみせた。\n"
                                + "「――これこれ。『大自然に囲まれた常夏の島々、小笠原諸島へ、高級客船で夢のクルージング八日間。二万九千八百円ポッキリ！』どうだ！」\n"
                                + "「おおっ、これは安いな」\n"
                                + "と、剣持。\n"
                                + "「同じ小笠原旅行でも、美雪の見つけてきたやつなんか十二万もするのに、これは四分の一以下だぜ。どう？　昼飯おごったかいあっただろ、オッサン」\n"
                                + "「ふーむ……なになに、東太平洋汽船？　聞いたこともない船会社だな。大丈夫なのか、金田一」\n"
                                + "「なーに、船なんて、どこの会社だっておんなしだろ」\n"
                                + "「し、しかし、このパンフレットもなんとなく印刷が悪いし、うす汚れてるぞ。それに、この『ポッキリ』ってのも気にならんか？　乗ったら船のマストがポッキリいきそうな……」\n"
                                + "「なんだよ、人がせっかく苦労して探してきたのに。だったらケチらずに、美雪が集めた高級リゾートツアーに、パーッと金出して行けばいいじゃん。公務員は不景気関係ないんだろ」\n"
                                + "「べつにおれはケチってるわけじゃないぞ。ただ似たような中身なら、安いにこしたことはなかろうと――」\n"
                                + "「まあまあ、警部」\n"
                                + "　美雪が割って入る。\n"
                                + "「――こういうのって気持ちの問題じゃないかしら。あたしが奥さんの立場なら、結婚十五周年の旅行が二万九千八百円だって知ったら、どんな内容のいいツアーだったとしてもやっぱりちょっといじけちゃうと思うな」\n"
                                + "「うーむ、そういうもんかな？」\n" + "「女にとって、記念日って、すごい大事な意味があるんですよ」\n"
                                + "「よし、わかった。七瀬くんの持ってきたツアーに決めよう。この沖縄石垣島十六万八千円ってやつ」\n" + "「わあー、すてき警部、太っ腹！」\n"
                                + "「わははは。おれだって、やる時はやるんだよ」\n" + "「ちぇっ、見栄はっちゃって。あとで泣きいれんなよ、オッサン」\n" + "「ばかもん。男に二言はない」\n"
                                + "と言って、剣持は鼻息を荒げて伝票をわしづかみにした。";
                rubys = Lists.newArrayList();
                rubys.add( new Ruby( "はじめ" , 171 , 1 ) );
                rubys.add( new Ruby( "はじめ" , 199 , 1 ) );
                rubys.add( new Ruby( "はじめ" , 235 , 1 ) );
                rubys.add( new Ruby( "しんきろう" , 480 , 3 ) );
                rubys.add( new Ruby( "ほ" , 1688 , 1 ) );
                rubys.add( new Ruby( "ふなべり" , 2574 , 2 ) );
                rubys.add( new Ruby( "ゴースト" , 2741 , 2 ) );
                rubys.add( new Ruby( "キヤプテン" , 2743 , 2 ) );
                rubys.add( new Ruby( "きんだいちはじめ" , 3306 , 4 ) );
                rubys.add( new Ruby( "ななせみゆき" , 3481 , 4 ) );
                rubys.add( new Ruby( "けんもちいさむ" , 4040 , 3 ) );
                rubys.add( new Ruby( "はじめ" , 4054 , 1 ) );
                rubys.add( new Ruby( "はじめ" , 4141 , 1 ) );
                rubys.add( new Ruby( "ふどうこうこう" , 4148 , 4 ) );
                rubys.add( new Ruby( "はじめ" , 4277 , 1 ) );
                rubys.add( new Ruby( "こうすけ" , 4281 , 2 ) );
                rubys.add( new Ruby( "はじめ" , 4448 , 1 ) );
                rubys.add( new Ruby( "はじめ" , 4512 , 1 ) );
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
