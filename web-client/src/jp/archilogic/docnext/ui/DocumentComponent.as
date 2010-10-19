package jp.archilogic.docnext.ui {
    import com.adobe.serialization.json.JSON;
    import flash.events.Event;
    import flash.events.KeyboardEvent;
    import flash.system.System;
    import mx.containers.Canvas;
    import mx.core.IIMESupport;
    import mx.events.FlexEvent;
    import jp.archilogic.docnext.dto.DocumentResDto;
    import jp.archilogic.docnext.helper.MouseActionHelper;
    import jp.archilogic.docnext.helper.ResizeHelper;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.util.DocumentLoadUtil;

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
        private var _pages : Array /* of PageComponent */;
        private var _dto : DocumentResDto;
        private var _info : Object;
        private var _baseScale : Number;
        private var _zoomExponent : int;
        private var _setPageHandler : Function;
        private var _isSelectingHandler : Function;
        private var _isSelectHighlightHandler : Function;
        private var _initHighlightCommentHandler : Function;

        private var _mouseActionHelper : MouseActionHelper;

        public function changeHighlightColor( color : uint ) : void {
            getCurrentPage().changeHighlightColor( color );
        }

        public function changeHighlightComment( comment : String ) : void {
            getCurrentPage().changeHighlightComment( comment );
        }

        public function changeToHighlight() : void {
            getCurrentPage().changeSelectionToHighlight();

            _isSelectingHandler( false );
        }

        public function copy() : void {
            if ( getCurrentPage().hasSelectedText() ) {
                System.setClipboard( getCurrentPage().selectedText );
            }
        }

        public function set initHighlightCommentHandler( value : Function ) : * {
            _initHighlightCommentHandler = value;
        }

        public function set isSelectHighlightHandler( value : Function ) : * {
            _isSelectHighlightHandler = value;
        }

        public function set isSelectingHandler( value : Function ) : * {
            _isSelectingHandler = _mouseActionHelper.isSelectingHandler = value;
        }

        public function load( dto : DocumentResDto ) : void {
            _dto = dto;

            DocumentService.getInfo( dto.id , function( json : String ) : void {
                _info = JSON.decode( json );

                _pages = [];

                loadPage( 0 , initLoadComplete );

                if ( _info.pages > 1 ) {
                    loadPage( 1 );
                }
            } );
        }

        public function set mouseModeHandler( value : Function ) : * {
            _mouseActionHelper.mouseModeHandler = value;
        }

        public function removeHighlight() : void {
            getCurrentPage().removeHighlight();
        }

        public function set setPageHandler( value : Function ) : * {
            _setPageHandler = value;
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

        private function changePage( pos : int ) : void {
            if ( pos < 0 || pos >= _info.pages || pos == _currentPos ) {
                return;
            }

            if ( !_pages[ pos ] ) {
                loadPage( pos , function( page : PageComponent ) : void {
                    changePage( pos );
                } );
                return;
            }

            var prev : PageComponent = getCurrentPage();

            currentPos = pos;
            var next : PageComponent = getCurrentPage();

            _ui.wrapper.addChild( next );
            next.scale = _ui.wrapper.scaleX;

            // for init
            if ( _ui.wrapper.contains( prev ) ) {
                _ui.wrapper.removeChild( prev );
            }

            next.initSelection();
            _isSelectingHandler( false );
            _isSelectHighlightHandler( false );

            if ( next.hasRegions() ) {
                next.clearEmphasize();
            } else {
                loadRegions();
            }

            loadNeighborPage();
        }

        private function changeScale() : void {
            var hPos : Number =
                _ui.scroller.maxHorizontalScrollPosition > 0 ? ( _ui.scroller.horizontalScrollPosition ) / _ui.scroller.maxHorizontalScrollPosition : 0.5;
            var vPos : Number =
                _ui.scroller.maxVerticalScrollPosition > 0 ? ( _ui.scroller.verticalScrollPosition ) / _ui.scroller.maxVerticalScrollPosition : 0.5;

            _ui.wrapper.scaleX = _ui.wrapper.scaleY = _baseScale * Math.pow( 2 , _zoomExponent / 3.0 );

            getCurrentPage().scale = _ui.wrapper.scaleX;

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

            _mouseActionHelper = new MouseActionHelper( _ui.scroller , systemManager , getCurrentPage );
        }

        private function set currentPos( value : int ) : * {
            _currentPos = value;
            _setPageHandler( _currentPos , _info.pages );
        }

        private function fitWrapperSize( page : PageComponent ) : void {
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

        private function getCurrentPage() : PageComponent {
            return _pages[ _currentPos ];
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
            return hasNext();
        }

        private function hasNext() : Boolean {
            return _currentPos < _info.pages - 1;
        }

        private function hasPrev() : Boolean {
            return _currentPos > 0;
        }

        private function hasRight() : Boolean {
            return hasPrev();
        }

        private function initLoadComplete( page : PageComponent ) : void {
            page.addEventListener( Event.ADDED_TO_STAGE , function() : void {
                page.removeEventListener( Event.ADDED_TO_STAGE , arguments.callee );

                page.callLater( function() : void {
                    currentPos = 0;
                    _zoomExponent = 0;
                    _isSelectingHandler( false );
                    _isSelectHighlightHandler( false );

                    fitWrapperSize( page );

                    loadRegions();
                } );
            } );

            _ui.wrapper.addChild( page );
        }

        private function loadNeighborPage() : void {
            var delta : int = 1;

            for ( var index : int = 0 ; index < _info.pages ; index++ ) {
                if ( index >= _currentPos - delta && index <= _currentPos + delta ) {
                    continue;
                }

                delete _pages[ index ];
            }

            if ( _currentPos + 1 < _info.pages && !_pages[ _currentPos + 1 ] ) {
                loadPage( _currentPos + 1 );
            }

            if ( _currentPos - 1 >= 0 && !_pages[ _currentPos - 1 ] ) {
                loadPage( _currentPos - 1 );
            }
        }

        private function loadPage( index : int , next : Function = null ) : void {
            DocumentLoadUtil.loadPage( _dto.id , index , _info.ratio , _pages , _isSelectHighlightHandler ,
                                       _initHighlightCommentHandler , _mouseActionHelper.mouseDownHandler , changePage ,
                                       next );
        }

        private function loadRegions() : void {
            DocumentLoadUtil.loadRegions( _dto.id , _currentPos , getCurrentPage() );
        }

        private function moveLeft() : void {
            moveToNextPage();
        }

        private function moveRight() : void {
            moveToPrevPage();
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

        private function resizeHandler() : void {
            if ( getCurrentPage() ) {
                fitWrapperSize( getCurrentPage() );
            }
        }
    }
}
