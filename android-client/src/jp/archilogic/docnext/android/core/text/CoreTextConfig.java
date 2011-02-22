package jp.archilogic.docnext.android.core.text;

public class CoreTextConfig {
    public static enum LineBreakingRule {
        NONE , TO_NEXT , SQUEEZE;
    }

    public int fontSize;
    public float lineSpace;
    public float horizontalPadding;
    public float verticalPadding;
    public boolean useJustification;
    public LineBreakingRule lineBreakingRule;

    // not popular config
    public int rubyFontSizeFactor;

    public CoreTextConfig() {
        fontSize = 16;
        lineSpace = 4;
        horizontalPadding = 32;
        verticalPadding = 24;
        useJustification = true;
        lineBreakingRule = LineBreakingRule.NONE;

        rubyFontSizeFactor = 2;
    }

    public int getRubyFontSize() {
        return fontSize / rubyFontSizeFactor;
    }
}
