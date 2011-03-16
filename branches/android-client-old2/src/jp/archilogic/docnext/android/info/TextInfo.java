package jp.archilogic.docnext.android.info;

import java.util.List;

public class TextInfo {
    public static class Dot {
        public int location;
        public int length;

        // for JSONIC
        public Dot() {
        }

        public Dot( final int location , final int length ) {
            this.location = location;
            this.length = length;
        }
    }

    public static class Ruby {
        public String text;
        public int location;
        public int length;

        // for JSONIC
        public Ruby() {
        }

        public Ruby( final String text , final int location , final int length ) {
            this.text = text;
            this.location = location;
            this.length = length;
        }
    }

    public static class TCY {
        public int location;
        public int length;

        // for JSONIC
        public TCY() {
        }

        public TCY( final int location , final int length ) {
            this.location = location;
            this.length = length;
        }
    }

    public String text;
    public List< Ruby > rubys;
    public List< Dot > dots;
    public List< TCY > tcys;

    // for JSONIC
    public TextInfo() {
    }

    public String at( final int index ) {
        return text.substring( index , index + 1 );
    }

    public int length() {
        return text.length();
    }
}
