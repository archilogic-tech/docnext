package jp.archilogic.docnext.android.coreview.text.engine;

import jp.archilogic.docnext.android.coreview.text.CoreTextConfig;
import jp.archilogic.docnext.android.coreview.text.TextLayoutInfo;
import jp.archilogic.docnext.android.info.SizeInfo;
import jp.archilogic.docnext.android.info.TextInfo;
import android.graphics.Canvas;
import android.graphics.Paint;

public interface CoreTextEngine {
    void drawDots( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final TextInfo source ,
            CoreTextConfig config );

    void drawRubys( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final TextInfo source ,
            CoreTextConfig config );

    void drawText( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final TextInfo source ,
            CoreTextConfig config );

    TextLayoutInfo[] layoutText( final Paint p , final TextInfo source , CoreTextConfig config , SizeInfo surface );
}
