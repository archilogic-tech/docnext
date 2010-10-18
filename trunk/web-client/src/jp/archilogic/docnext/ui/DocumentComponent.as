package jp.archilogic.docnext.ui {
    import com.adobe.serialization.json.JSON;
    import flash.events.Event;
    import flash.events.KeyboardEvent;
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.system.System;
    import flash.utils.ByteArray;
    import flash.utils.Endian;
    import mx.containers.Canvas;
    import mx.core.IIMESupport;
    import mx.events.FlexEvent;
    import jp.archilogic.docnext.dto.DocumentResDto;
    import jp.archilogic.docnext.helper.PageLoadHelper;
    import jp.archilogic.docnext.helper.ResizeHelper;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.type.MouseMode;

    public class DocumentComponent extends Canvas {
        public function DocumentComponent() {
            super();

            _ui = new DocumentComponentUI();
            _ui.addEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );
            addChild( _ui );

            addEventListener( Event.ADDED_TO_STAGE , addToStageHandler );
        }

        private var _ui : DocumentComponentUI;
        private var _currentPos : int;
        private var _pageImages : Array; /* of Image */
        private var _dto : DocumentResDto;
        private var _info : Object;
        private var _text : String;
        private var _pageLoadHelper : PageLoadHelper;
        private var _baseScale : Number;
        private var _zoomExponent : int;
        private var _mouseModeHandler : Function;
        private var _isSelectingHandler : Function;
        private var _isSelectHighlightHandler : Function;
        private var _initHighlightCommentHandler : Function;

        public function changeHighlightColor( color : uint ) : void {
            var current : TiledLoader = _pageImages[ _currentPos ];
            current.changeHighlightColor( color );
        }

        public function changeHighlightComment( comment : String ) : void {
            var current : TiledLoader = _pageImages[ _currentPos ];
            current.changeHighlightComment( comment );
        }

        public function changeToHighlight() : void {
            var current : TiledLoader = _pageImages[ _currentPos ];
            current.changeSelectionToHighlight();

            _isSelectingHandler( false );
        }

        public function copy() : void {
            var current : TiledLoader = _pageImages[ _currentPos ];

            if ( current.hasSelectedText() ) {
                System.setClipboard( current.selectedText );
            }
        }

        public function set initHighlightCommentHandler( value : Function ) : * {
            _initHighlightCommentHandler = value;
        }

        public function set isSelectHighlightHandler( value : Function ) : * {
            _isSelectHighlightHandler = value;
        }

        public function set isSelectingHandler( value : Function ) : * {
            _isSelectingHandler = value;
        }

        public function load( dto : DocumentResDto ) : void {
            _dto = dto;

            var self : DocumentComponent = this;
            DocumentService.getInfo( dto.id , function( json : String ) : void {
                _info = JSON.decode( json );

                _pageImages = [];

                for ( var index : int = 0 ; index < _info.pages ; index++ ) {
                    _pageImages[ index ] = new TiledLoader();
                    _pageImages[ index ].docId = _dto.id;
                    _pageImages[ index ].page = index;
                    _pageImages[ index ].ratio = _info.ratio;
                    _pageImages[ index ].isSelectHighlightHandler = _isSelectHighlightHandler;
                    _pageImages[ index ].initHighlightCommentHandler = _initHighlightCommentHandler;
                    _pageImages[ index ].addEventListener( MouseEvent.MOUSE_DOWN , mouseDownHandler );
                }

                _pageLoadHelper =
                    new PageLoadHelper( self , _info.pages , _dto.id , _pageImages , _ui.wrapper ,
                                        pageLoaderHelperInitLoadCompleteHanlder );
                _pageLoadHelper.start();
            } );
        }

        public function set mouseModeHandler( value : Function ) : * {
            _mouseModeHandler = value;
        }

        public function removeHighlight() : void {
            var current : TiledLoader = _pageImages[ _currentPos ];
            current.removeHighlight();
        }

        public function stopLoading() : void {
            _pageLoadHelper.needStopLoading();
        }

        public function zoomIn() : void {
            _zoomExponent = Math.min( _zoomExponent + 1 , 6 );

            changeScale();
        }

        public function zoomOut() : void {
            _zoomExponent = Math.max( _zoomExponent - 1 , 0 );

            changeScale();
        }

        private function addToStageHandler( e : Event ) : void {
            stage.addEventListener( KeyboardEvent.KEY_UP , myKeyUpHandler );
        }

        private function arrowIndicatorClickHanlder( delta : int ) : void {
            if ( delta < 0 ) {
                moveLeft();
            } else {
                moveRight();
            }
        }

        private function centering() : void {
            _ui.wrapper.x = Math.max( ( _ui.scroller.width - _ui.wrapper.width ) / 2 , 0 );
            _ui.wrapper.y = Math.max( ( _ui.scroller.height - _ui.wrapper.height ) / 2 , 0 );
        }

        private function changePage( pos : int , fromThumb : Boolean = false ) : void {
            if ( pos < 0 || pos >= _pageImages.length || pos > _pageLoadHelper.loadedPos || pos == _currentPos ) {
                return;
            }

            var current : TiledLoader = _pageImages[ _currentPos ];
            var next : TiledLoader = _pageImages[ pos ];

            _ui.wrapper.addChild( next );
            next.scale = _ui.wrapper.scaleX;

            // for init
            if ( _ui.wrapper.contains( current ) ) {
                _ui.wrapper.removeChild( current );
            }

            _currentPos = pos;

            next.initSelection();
            _isSelectingHandler( false );
            _isSelectHighlightHandler( false );

            if ( next.hasRegions() ) {
                next.clearEmphasize();
            } else {
                loadRegions();
            }
        }

        private function changeScale() : void {
            var hPos : Number =
                _ui.scroller.maxHorizontalScrollPosition > 0 ? ( _ui.scroller.horizontalScrollPosition ) / _ui.scroller.maxHorizontalScrollPosition : 0.5;
            var vPos : Number =
                _ui.scroller.maxVerticalScrollPosition > 0 ? ( _ui.scroller.verticalScrollPosition ) / _ui.scroller.maxVerticalScrollPosition : 0.5;

            _ui.wrapper.scaleX = _ui.wrapper.scaleY = _baseScale * Math.pow( 2 , _zoomExponent / 3.0 );

            var current : TiledLoader = _pageImages[ _currentPos ];
            current.scale = _ui.wrapper.scaleX;

            _ui.wrapper.callLater( function() : void {
                centering();

                _ui.scroller.horizontalScrollPosition = hPos * _ui.scroller.maxHorizontalScrollPosition;
                _ui.scroller.verticalScrollPosition = vPos * _ui.scroller.maxVerticalScrollPosition;
            } );
        }


        private function creationCompleteHandler( e : FlexEvent ) : void {
            _ui.removeEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );

            _ui.arrowIndicator.topPosFunc = getTopPos;
            _ui.arrowIndicator.bottomPosFunc = getBottomPos;
            _ui.arrowIndicator.leftPosFunc = getLeftPos;
            _ui.arrowIndicator.rightPosFunc = getRightPos;
            _ui.arrowIndicator.clickHandler = arrowIndicatorClickHanlder;
            _ui.arrowIndicator.hasLeftFunc = hasLeft;
            _ui.arrowIndicator.hasRightFunc = hasRight;

            new ResizeHelper( this , resizeHandler );
        }

        private function fitWrapperSize( page : TiledLoader ) : void {
            if ( _ui.width / page.content.width > _ui.height / page.content.height ) {
                // fit to height
                _baseScale = _ui.height / page.content.height;
            } else {
                // fit to width
                _baseScale = _ui.width / page.content.width;
            }

            changeScale();
        }

        private function getBottomPos() : Number {
            return Math.min( _ui.scroller.height , _ui.wrapper.y + _ui.wrapper.height );
        }

        private function getLeftPos() : Number {
            return _ui.wrapper.x;
        }

        private function getRightPos() : Number {
            return Math.min( _ui.scroller.width , _ui.wrapper.x + _ui.wrapper.width );
        }

        private function getTopPos() : Number {
            return _ui.wrapper.y;
        }

        private function hasLeft() : Boolean {
            return _currentPos > 0;
        }

        private function hasRight() : Boolean {
            return _currentPos < Math.min( _pageImages.length - 1 , _pageLoadHelper.loadedPos );
        }

        private function loadImageText() : void {
            DocumentService.getImageText( _dto.id , _currentPos , function( text : String ) : void {
                var current : TiledLoader = _pageImages[ _currentPos ];
                current.text = text;
            } );
        }

        private function loadRegions() : void {
            DocumentService.getRegions( _dto.id , _currentPos , function( regions : ByteArray ) : void {
                regions.endian = Endian.LITTLE_ENDIAN;

                var regions_ : Array = [];

                while ( regions.position < regions.length ) {
                    var region : Rectangle =
                        new Rectangle( regions.readDouble() , regions.readDouble() , regions.readDouble() ,
                                       regions.readDouble() );

                    regions_.push( region );
                }

                var current : TiledLoader = _pageImages[ _currentPos ];

                current.regions = regions_;
                current.loadState();

                loadImageText();
            } );
        }

        private function mouseDownHandler( e : MouseEvent ) : void {
            if ( _mouseModeHandler() == MouseMode.SCROLL ) {
                mouseDownHandlerOnScroll( e );
            } else if ( _mouseModeHandler() == MouseMode.SELECT ) {
                mouseDownHandlerOnSelect( e );
            }
        }

        private function mouseDownHandlerOnScroll( e : MouseEvent ) : void {
            var mousePoint : Point = new Point( e.stageX , e.stageY );
            var scrollPoint : Point =
                new Point( _ui.scroller.horizontalScrollPosition , _ui.scroller.verticalScrollPosition );

            systemManager.addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
            systemManager.addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );

            function mouseMoveHandler( _e : MouseEvent ) : void {
                _ui.scroller.horizontalScrollPosition = scrollPoint.x - _e.stageX + mousePoint.x;
                _ui.scroller.verticalScrollPosition = scrollPoint.y - _e.stageY + mousePoint.y;
            }

            function mouseUpHandler( _e : MouseEvent ) : void {
                systemManager.removeEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                systemManager.removeEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
            }
        }

        private function mouseDownHandlerOnSelect( e : MouseEvent ) : void {
            var current : TiledLoader = _pageImages[ _currentPos ];

            var edgeIndex : int = current.getNearTextPos( current.globalToLocal( new Point( e.stageX , e.stageY ) ) );
            current.initSelection();

            systemManager.addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
            systemManager.addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );

            _isSelectingHandler( false );

            function mouseMoveHandler( _e : MouseEvent ) : void {
                var index : int = current.getNearTextPos( current.globalToLocal( new Point( _e.stageX , _e.stageY ) ) );
                current.showSelection( Math.min( edgeIndex , index ) , Math.max( edgeIndex , index ) );

                _isSelectingHandler( true );
            }

            function mouseUpHandler( _e : MouseEvent ) : void {
                systemManager.removeEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                systemManager.removeEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
            }
        }

        private function moveLeft() : void {
            moveToPrevPage();
        }

        private function moveRight() : void {
            moveToNextPage();
        }

        private function moveToNextPage() : void {
            changePage( _currentPos + 1 );
        }

        private function moveToPrevPage() : void {
            changePage( _currentPos - 1 );
        }

        private function myKeyUpHandler( e : KeyboardEvent ) : void {
            if ( !( e.target is IIMESupport ) ) {
                if ( e.keyCode == 'J'.charCodeAt( 0 ) ) {
                    moveLeft();
                } else if ( e.keyCode == 'K'.charCodeAt( 0 ) ) {
                    moveRight();
                }
            }
        }

        private function pageLoaderHelperInitLoadCompleteHanlder( page : TiledLoader ) : void {
            _currentPos = 0;
            _zoomExponent = 0;
            _isSelectingHandler( false );
            _isSelectHighlightHandler( false );

            fitWrapperSize( page );

            loadRegions();
        }

        private function resizeHandler() : void {
            if ( _pageImages ) {
                fitWrapperSize( _pageImages[ _currentPos ] );
            }
        }
    }
}
