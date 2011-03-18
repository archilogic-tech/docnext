package jp.archilogic.docnext.android.coreview.image;

import java.util.List;

import android.graphics.Bitmap;

import com.google.common.collect.Lists;

public class PageImageCache {
    int page;
    List< Bitmap > bitmaps;

    public PageImageCache( final int page ) {
        this.page = page;
        bitmaps = Lists.newArrayList();
    }
}
