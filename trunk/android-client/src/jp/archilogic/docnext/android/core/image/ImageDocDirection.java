package jp.archilogic.docnext.android.core.image;

import jp.archilogic.docnext.android.core.Size;
import android.graphics.PointF;

public enum ImageDocDirection {
    L2R , R2L , T2B , B2T;

    private static final int PAGE_CHANGE_THREASHOLD = 4;

    boolean canMoveHorizontal() {
        switch ( this ) {
        case L2R:
        case R2L:
            return true;
        case T2B:
        case B2T:
            return false;
        default:
            throw new RuntimeException();
        }
    }

    boolean canMoveVertical() {
        return !canMoveHorizontal();
    }

    boolean shouldChangeToNext( final PointF offset , final Size surface , final Size image , final float scale ) {
        switch ( this ) {
        case L2R:
            return offset.x < surface.width - surface.width / PAGE_CHANGE_THREASHOLD - image.width * scale;
        case R2L:
            return offset.x > surface.width / PAGE_CHANGE_THREASHOLD;
        case T2B:
            return offset.y < surface.height - surface.height / PAGE_CHANGE_THREASHOLD - image.height * scale;
        case B2T:
            return offset.y > surface.height / PAGE_CHANGE_THREASHOLD;
        default:
            throw new RuntimeException();
        }
    }

    boolean shouldChangeToPrev( final PointF offset , final Size surface , final Size image , final float scale ) {
        switch ( this ) {
        case L2R:
            return offset.x > surface.width / PAGE_CHANGE_THREASHOLD;
        case R2L:
            return offset.x < surface.width - surface.width / PAGE_CHANGE_THREASHOLD - image.width * scale;
        case T2B:
            return offset.y > surface.height / PAGE_CHANGE_THREASHOLD;
        case B2T:
            return offset.y < surface.height - surface.height / PAGE_CHANGE_THREASHOLD - image.height * scale;
        default:
            throw new RuntimeException();
        }
    }

    int toXSign() {
        switch ( this ) {
        case L2R:
            return 1;
        case R2L:
            return -1;
        case T2B:
        case B2T:
            return 0;
        default:
            throw new RuntimeException();
        }
    }

    int toYSign() {
        switch ( this ) {
        case L2R:
        case R2L:
            return 0;
        case T2B:
            return 1;
        case B2T:
            return -1;
        default:
            throw new RuntimeException();
        }
    }

    void updateOffset( final PointF offset , final Size image , final float scale , final boolean isNext ) {
        final int sign = ( this == L2R || this == T2B ) ^ isNext ? -1 : 1;

        switch ( this ) {
        case L2R:
        case R2L:
            offset.x += sign * image.width * scale;
            break;
        case T2B:
        case B2T:
            offset.y += sign * image.height * scale;
            break;
        default:
            throw new RuntimeException();
        }
    }
}
