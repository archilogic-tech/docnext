package jp.archilogic.docnext.android.core.text;

import java.util.List;

public class CoreTextInfo {
    public static class Dot {
        public int location;
        public int length;

        public Dot( final int location , final int length ) {
            this.location = location;
            this.length = length;
        }
    }

    public static class Ruby {
        public String text;
        public int location;
        public int length;

        public Ruby( final String text , final int location , final int length ) {
            this.text = text;
            this.location = location;
            this.length = length;
        }
    }

    public String text;
    public List< Ruby > rubys;
    public List< Dot > dots;

    public String at( final int index ) {
        return text.substring( index , index + 1 );
    }

    public int length() {
        return text.length();
    }
}
