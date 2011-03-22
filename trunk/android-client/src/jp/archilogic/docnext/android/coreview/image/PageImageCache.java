package jp.archilogic.docnext.android.coreview.image;

import android.graphics.Bitmap;

class PageImageCache {
    int page;
    int px;
    int py;
    Bitmap bitmap;

    PageImageCache( final int page , final int px , final int py , final Bitmap bitmap ) {
        this.page = page;
        this.px = px;
        this.py = py;
        this.bitmap = bitmap;
    }
}
