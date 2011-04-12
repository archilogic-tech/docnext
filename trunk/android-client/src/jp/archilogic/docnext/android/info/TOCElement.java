package jp.archilogic.docnext.android.info;


public class TOCElement implements Comparable< TOCElement > {
	public String text;
	public int page;

    @Override
    public int compareTo( TOCElement another ) {
        return page - another.page;
    }
}
