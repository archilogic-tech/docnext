package jp.archilogic.docnext.helper {
    import flash.events.Event;
    import mx.collections.ArrayCollection;
    import mx.containers.Canvas;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.ui.DocumentComponent;
    import jp.archilogic.docnext.ui.LoadingBox;
    import jp.archilogic.docnext.ui.TiledLoader;

    public class PageLoadHelper {
        public function PageLoadHelper( parent : DocumentComponent , count : int , docId : Number , pageImages : Array ,
                                        wrapper : Canvas , initLoadCompleteHandler : Function ) {
            _parent = parent;
            _count = count;
            _docId = docId;
            _pageImages = pageImages;
            _wrapper = wrapper;
            _initLoadCompleteHandler = initLoadCompleteHandler;
        }

        private var _parent : DocumentComponent;
        private var _count : int;
        private var _docId : Number;
        private var _pageImages : Array;
        private var _wrapper : Canvas;
        private var _initLoadCompleteHandler : Function;

        private var _loadedPos : uint;
        private var _needStopLoading : Boolean;

        public function get loadedPos() : uint {
            return _loadedPos;
        }

        public function needStopLoading() : void {
            _needStopLoading = true;
        }

        public function start() : void {
            var loadingBox : LoadingBox = new LoadingBox( _count );
            _parent.addChild( loadingBox );

            _needStopLoading = false;
            _loadedPos = 0;
            loadFromServer( _docId , _count , 0 , loadingBox );
        }

        private function loadFromServer( id : Number , count : int , pos : int , loading : LoadingBox ) : void {
            if ( _needStopLoading ) {
                _needStopLoading = false;
                _parent.removeChild( loading );
                return;
            }

            if ( pos < count ) {
                loading.current = pos;

                DocumentService.getPage( id , pos , function( result : ArrayCollection ) : void {
                    loadToLocal( pos , result );

                    loadFromServer( id , count , pos + 1 , loading );
                } );
            } else {
                _parent.removeChild( loading );
            }
        }

        private function loadToLocal( pos : int , data : ArrayCollection ) : void {
            var image : TiledLoader = _pageImages[ pos ];

            image.addEventListener( Event.COMPLETE , function() : void {
                image.removeEventListener( Event.COMPLETE , arguments.callee );

                if ( pos == 0 ) {
                    image.addEventListener( Event.ADDED_TO_STAGE , function() : void {
                        image.removeEventListener( Event.ADDED_TO_STAGE , arguments.callee );

                        image.callLater( function() : void {
                            _initLoadCompleteHandler( image );
                        } );
                    } );

                    _wrapper.addChild( image );
                }

                _loadedPos = Math.max( _loadedPos , pos );
            } );

            image.loadData( data );
        }
    }
}