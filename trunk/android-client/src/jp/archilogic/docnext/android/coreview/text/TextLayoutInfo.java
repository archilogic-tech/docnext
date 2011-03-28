package jp.archilogic.docnext.android.coreview.text;

public class TextLayoutInfo {
    public float x;
    public float y;
    public float width;
    public float height;

    public int line;
    public float lineMetrics;

    public TextLayoutInfo( final float x , final float y , final float width , final float height , final int line ,
            final float lineMetrics ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.line = line;
        this.lineMetrics = lineMetrics;
    }
}
