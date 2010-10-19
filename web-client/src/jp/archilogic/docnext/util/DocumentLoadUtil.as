package jp.archilogic.docnext.util {
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.geom.Rectangle;
    import flash.utils.ByteArray;
    import flash.utils.Endian;
    import mx.collections.ArrayCollection;
    import mx.core.Container;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.ui.TiledLoader;

    public class DocumentLoadUtil {
        public static function loadPage( docId : Number , index : int , ratio : Number , pages : Array ,
                                         isSelectHighlightHandler : Function , initHighlightCommentHandler : Function ,
                                         mouseDownHandler : Function , initLoadCompleteHandler : Function = null ,
                                         wrapper : Container = null ) : void {
            DocumentService.getPage( docId , index , function( result : ArrayCollection ) : void {
                var page : TiledLoader = new TiledLoader();
                page.docId = docId;
                page.page = index;
                page.ratio = ratio;
                page.isSelectHighlightHandler = isSelectHighlightHandler;
                page.initHighlightCommentHandler = initHighlightCommentHandler;
                page.addEventListener( MouseEvent.MOUSE_DOWN , mouseDownHandler );

                page.addEventListener( Event.COMPLETE , function() : void {
                    page.removeEventListener( Event.COMPLETE , arguments.callee );

                    pages[ index ] = page;

                    if ( initLoadCompleteHandler != null ) {
                        page.addEventListener( Event.ADDED_TO_STAGE , function() : void {
                            page.removeEventListener( Event.ADDED_TO_STAGE , arguments.callee );

                            page.callLater( function() : void {
                                initLoadCompleteHandler( page );
                            } );
                        } );

                        wrapper.addChild( page );
                    }
                } );

                page.loadData( result );
            } );
        }

        public static function loadRegions( docId : Number , currentIndex : int , currentPage : TiledLoader ) : void {
            DocumentService.getRegions( docId , currentIndex , function( result : ByteArray ) : void {
                result.endian = Endian.LITTLE_ENDIAN;

                var regions : Array = [];

                while ( result.position < result.length ) {
                    var region : Rectangle =
                        new Rectangle( result.readDouble() , result.readDouble() , result.readDouble() ,
                                       result.readDouble() );

                    regions.push( region );
                }

                currentPage.regions = regions;
                currentPage.loadState();

                loadImageText( docId , currentIndex , currentPage );
            } );
        }

        private static function loadImageText( docId : Number , currentIndex : int , currentPage : TiledLoader ) : void {
            DocumentService.getImageText( docId , currentIndex , function( text : String ) : void {
                currentPage.text = text;
            } );
        }
    }
}