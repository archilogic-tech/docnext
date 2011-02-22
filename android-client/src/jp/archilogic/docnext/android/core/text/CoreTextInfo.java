package jp.archilogic.docnext.android.core.text;

import java.util.List;

public class CoreTextInfo {
    public static class Ruby {
        public String text;
        public int location;
        public int length;
    }

    public String text;
    public List< Ruby > rubys;

    public String at( final int index ) {
        return text.substring( index , index + 1 );
    }

    public int length() {
        return text.length();
    }
}
