package jp.archilogic.docnext.android.coreview;

import jp.archilogic.docnext.android.meta.DocumentType;
import android.content.Intent;

public interface CoreViewDelegate {
    void changeCoreViewType( DocumentType type , Intent data );

    void goBack();
}
