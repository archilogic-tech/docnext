package jp.archilogic.docnext.android.core.text;

import jp.archilogic.docnext.android.core.Size;

class TextCleanUpState {
    public static TextCleanUpState getInstance( final TextDocDirection direction , final float offset ,
            final Size surface , final Size cache , final int index , final int cacheCount ) {
        final TextCleanUpState ret = new TextCleanUpState();

        if ( index == 0 ) {
            switch ( direction ) {
            case HORIZONTAL:
                if ( offset > 0 ) {
                    ret.needCleanUp = true;
                    ret.srcOffset = offset;
                    ret.dstOffset = 0;
                }

                break;
            case VERTICAL:
                if ( offset < surface.width - cache.width ) {
                    ret.needCleanUp = true;
                    ret.srcOffset = offset;
                    ret.dstOffset = surface.width - cache.width;
                }

                break;
            default:
                throw new RuntimeException();
            }
        } else if ( index == cacheCount - 1 ) {
            switch ( direction ) {
            case HORIZONTAL:
                if ( offset < surface.height - cache.height ) {
                    ret.needCleanUp = true;
                    ret.srcOffset = offset;
                    ret.dstOffset = surface.height - cache.height;
                }

                break;
            case VERTICAL:
                if ( offset > 0 ) {
                    ret.needCleanUp = true;
                    ret.srcOffset = offset;
                    ret.dstOffset = 0;
                }

                break;
            default:
                throw new RuntimeException();
            }
        }

        return ret;
    }

    boolean needCleanUp;

    float srcOffset;
    float dstOffset;
}
