package jp.archilogic.docnext.util {
    import com.adobe.serialization.json.JSON;
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.geom.Rectangle;
    import flash.utils.ByteArray;
    import flash.utils.Endian;
    import __AS3__.vec.Vector;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.ui.PageComponent;

    public class DocumentLoadUtil {
        public static function loadPage( docId : Number , index : int , ratio : Number ,
                                         pages : Vector.<PageComponent> , isSelectHighlightHandler : Function ,
                                         initHighlightCommentHandler : Function , mouseDownHandler : Function ,
                                         changePageHandler : Function , loadCompleteHandler : Function = null ) : void {
            DocumentService.getPage( docId , index , function( result : ByteArray ) : void {
                var page : PageComponent = new PageComponent();
                page.docId = docId;
                page.page = index;
                page.ratio = ratio;
                page.isSelectHighlightHandler = isSelectHighlightHandler;
                page.initHighlightCommentHandler = initHighlightCommentHandler;
                page.changePageHandler = changePageHandler;
                page.addEventListener( MouseEvent.MOUSE_DOWN , mouseDownHandler );

                page.addEventListener( Event.COMPLETE , function() : void {
                    page.removeEventListener( Event.COMPLETE , arguments.callee );

                    pages[ index ] = page;

                    if ( loadCompleteHandler != null ) {
                        loadCompleteHandler( page );
                    }
                } );

                page.loadData( result );
            } );
        }

        public static function loadRegions( docId : Number , currentIndex : int , currentPage : PageComponent ) : void {
            DocumentService.getRegions( docId , currentIndex , function( result : ByteArray ) : void {
                result.endian = Endian.LITTLE_ENDIAN;

                var regions : Vector.<Rectangle> = new Vector.<Rectangle>();

                while ( result.position < result.length ) {
                    var region : Rectangle =
                        new Rectangle( result.readDouble() , result.readDouble() , result.readDouble() ,
                                       result.readDouble() );

                    regions.push( region );
                }

                currentPage.regions = regions;

                loadImageText( docId , currentIndex , currentPage );
            } );
        }

        private static function loadAnnotation( docId : Number , currentIndex : int ,
                                                currentPage : PageComponent ) : void {
            DocumentService.getAnnotation( docId , currentIndex , function( result : String ) : void {
                currentPage.annotation = JSON.decode( result );
            } );
        }

        private static function loadImageText( docId : Number , currentIndex : int ,
                                               currentPage : PageComponent ) : void {
            DocumentService.getImageText( docId , currentIndex , function( text : String ) : void {
                currentPage.text = text;

                loadAnnotation( docId , currentIndex , currentPage );
            } );
        }
    }
}