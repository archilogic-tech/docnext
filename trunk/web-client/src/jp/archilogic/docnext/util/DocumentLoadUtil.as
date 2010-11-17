package jp.archilogic.docnext.util {
    import com.adobe.serialization.json.JSON;
    import flash.events.Event;
    import flash.geom.Rectangle;
    import flash.utils.ByteArray;
    import flash.utils.Endian;
    import mx.containers.Canvas;
    import __AS3__.vec.Vector;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.ui.PageComponent;

    public class DocumentLoadUtil {
        public static function loadPage( docId : Number , index : int , ratio : Number ,
                                         pages : Vector.<PageComponent> , scroller : Canvas ,
                                         isSelectHighlightHandler : Function , initHighlightCommentHandler : Function ,
                                         currentTargetHandler : Function , changePageHandler : Function ,
                                         loadCompleteHandler : Function = null ) : void {
            DocumentService.getPage( docId , index , function( result : ByteArray ) : void {
                var page : PageComponent = new PageComponent( scroller );
                page.docId = docId;
                page.page = index;
                page.ratio = ratio;
                page.isSelectHighlightHandler = isSelectHighlightHandler;
                page.initHighlightCommentHandler = initHighlightCommentHandler;
                page.changePageHandler = changePageHandler;
                page.currentTargetHandler = currentTargetHandler;

                page.addEventListener( Event.COMPLETE , function() : void {
                    page.removeEventListener( Event.COMPLETE , arguments.callee );

                    pages[ index ] = page;

                    loadRegions( page );

                    if ( loadCompleteHandler != null ) {
                        loadCompleteHandler( page );
                    }
                } );

                page.loadData( result );
            } );
        }

        private static function loadAnnotation( page : PageComponent ) : void {
            DocumentService.getAnnotation( page.docId , page.page , function( result : String ) : void {
                page.annotation = JSON.decode( result );
            } );
        }

        private static function loadImageText( page : PageComponent ) : void {
            DocumentService.getImageText( page.docId , page.page , function( text : String ) : void {
                page.text = text;

                loadAnnotation( page );
            } );
        }

        private static function loadRegions( page : PageComponent ) : void {
            DocumentService.getRegions( page.docId , page.page , function( result : ByteArray ) : void {
                result.endian = Endian.LITTLE_ENDIAN;

                var regions : Vector.<Rectangle> = new Vector.<Rectangle>();

                while ( result.position < result.length ) {
                    var region : Rectangle =
                        new Rectangle( result.readDouble() , result.readDouble() , result.readDouble() ,
                                       result.readDouble() );

                    regions.push( region );
                }

                page.regions = regions;

                loadImageText( page );
            } );
        }
    }
}