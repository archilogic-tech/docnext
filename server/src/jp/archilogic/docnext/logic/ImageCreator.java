package jp.archilogic.docnext.logic;

import jp.archilogic.docnext.logic.ThumbnailCreator.CreateResult;

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
	CreateResult create( String outDir , String pdfPath , String prefix , long id );

    int getPages( String pdfPath );
}
