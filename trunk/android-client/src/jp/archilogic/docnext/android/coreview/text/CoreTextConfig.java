package jp.archilogic.docnext.android.coreview.text;

import jp.archilogic.docnext.android.setting.Setting;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.TypedValue;

public class CoreTextConfig {
    public static enum LineBreakingRule {
        NONE , TO_NEXT , SQUEEZE;
    }

    // typesettings
    public TextDocDirection direction;
    public int fontSize; // unit is piont( not sp )
    public float lineSpace;
    public float horizontalPadding;
    public float verticalPadding;
    public boolean useJustification;
    public LineBreakingRule lineBreakingRule;

    // viewer config
    public float pageSpace;
    public int backgroundColor;
    public int defaultTextColor;

    // not popular config
    public int rubyFontSizeFactor;
    
    public Context _context;

    CoreTextConfig( Context context ) {
        _context = context;
        
        direction = TextDocDirection.HORIZONTAL;
        fontSize = 16;
        lineSpace = 4;
        horizontalPadding = 32;
        verticalPadding = 24;
        useJustification = true;
        lineBreakingRule = LineBreakingRule.NONE;

        pageSpace = 32;
        backgroundColor = Color.WHITE;
        defaultTextColor = 0xff222222;

        rubyFontSizeFactor = 2;
        
        SharedPreferences prefs = context.getSharedPreferences( "viewer" , Context.MODE_PRIVATE );
        fontSize = prefs.getInt( Setting.PREF_FONT_SIZE , Setting.DEFAULT_FONT_SIZE );
        
        int sp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                (float) fontSize , _context.getResources().getDisplayMetrics());

        fontSize = sp;
        
        backgroundColor = prefs.getInt( Setting.PREF_BACKGROUND_COLOR , Setting.DEFAULT_BACKGROUND_COLOR );
        defaultTextColor = prefs.getInt( Setting.PREF_FONT_COLOR , Setting.DEFAULT_FONT_COLOR );
        String writingMode = prefs.getString( Setting.PREF_WRITING_MODE , Setting.DEFAULT_WRITING_MODE );
        if ( writingMode.equals( Setting.VERTICAL ) ) {
            direction = TextDocDirection.VERTICAL;
        } else {
            direction = TextDocDirection.HORIZONTAL;
        }
    }

    public int getRubyFontSize() {
        return fontSize / rubyFontSizeFactor;
    }
}
