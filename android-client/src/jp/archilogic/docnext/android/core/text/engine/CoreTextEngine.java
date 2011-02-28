package jp.archilogic.docnext.android.core.text.engine;

import jp.archilogic.docnext.android.core.Size;
import jp.archilogic.docnext.android.core.text.CoreTextConfig;
import jp.archilogic.docnext.android.core.text.CoreTextInfo;
import jp.archilogic.docnext.android.core.text.TextLayoutInfo;
import android.graphics.Canvas;
import android.graphics.Paint;

public interface CoreTextEngine {
    void drawDots( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final CoreTextInfo source ,
            CoreTextConfig config );

    void drawRubys( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final CoreTextInfo source ,
            CoreTextConfig config );

    void drawText( final Canvas c , final Paint p , final TextLayoutInfo[] layouts , final CoreTextInfo source ,
            CoreTextConfig config );

    TextLayoutInfo[] layoutText( final Paint p , final CoreTextInfo source , CoreTextConfig config , Size surface );
}
