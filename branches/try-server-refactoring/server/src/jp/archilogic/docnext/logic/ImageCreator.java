package jp.archilogic.docnext.logic;

/**
 * pdfファイルからjpgファイル群を生成する処理インタフェース
 * 
 * applicationContext-etc.xmlなどでコンポーネントを指定する。
 * <pre>
 *  <bean id="imageCreator"
 *       class="jp.archilogic.docnext.logic.ThumbnailCreator" />
 * </pre>
 *
 */
public interface ImageCreator {
    double create( String outDir , String pdfPath , String prefix , long id );

    int getPages( String pdfPath );
}
