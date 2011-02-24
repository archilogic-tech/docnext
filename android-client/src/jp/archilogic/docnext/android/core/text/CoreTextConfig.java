package jp.archilogic.docnext.android.core.text;

import android.graphics.Color;

public class CoreTextConfig {
    public static enum LineBreakingRule {
        NONE , TO_NEXT , SQUEEZE;
    }

    // typesettings
    public int fontSize;
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

    public CoreTextConfig() {
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
    }

    public int getRubyFontSize() {
        return fontSize / rubyFontSizeFactor;
    }
}
