package jp.archilogic.docnext.ui {
    import com.adobe.serialization.json.JSON;
    import com.foxaweb.pageflip.PageFlip;
    import flash.display.Shape;
    import flash.events.Event;
    import flash.events.KeyboardEvent;
    import flash.geom.Point;
    import flash.system.System;
    import mx.containers.Canvas;
    import mx.core.IIMESupport;
    import mx.core.UIComponent;
    import mx.events.FlexEvent;
    import __AS3__.vec.Vector;
    import jp.archilogic.docnext.dto.DocumentResDto;
    import jp.archilogic.docnext.helper.MouseActionHelper;
    import jp.archilogic.docnext.helper.PageHeadHelper;
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
        private var _currentHead : int;
        private var _pages : Vector.<PageComponent> /* of PageComponent */;
        private var _dto : DocumentResDto;
        private var _info : Object;
        private var _pageHeadHelper : PageHeadHelper;
        private var _baseScale : Number;
        private var _zoomExponent : int;
        private var _setPageHandler : Function;
        private var _isSelectingHandler : Function;
        private var _isSelectHighlightHandler : Function;
        private var _initHighlightCommentHandler : Function;
        private var _mouseActionHelper : MouseActionHelper;
        private var _isAnimating : Boolean;

        public function changeHighlightColor( color : uint ) : void {
            getCurrentForePage().changeHighlightColor( color );
        }

        public function changeHighlightComment( comment : String ) : void {
            getCurrentForePage().changeHighlightComment( comment );
        }

        public function changeToHighlight() : void {
            getCurrentForePage().changeSelectionToHighlight();

            _isSelectingHandler( false );
        }

        public function copy() : void {
            if ( getCurrentForePage().hasSelectedText() ) {
                System.setClipboard( getCurrentForePage().selectedText );
            }
        }

        public function set initHighlightCommentHandler( value : Function ) : * {
            _initHighlightCommentHandler = value;
        }

        public function set isAnimating( value : Boolean ) : * {
            _isAnimating = value;

            if ( _isAnimating ) {
                _ui.arrowIndicator.startAnimating();
            }
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

                // TODO load singlePageInfo
                _pageHeadHelper = new PageHeadHelper( [ 0 ] , _info.pages );

                _pages = new Vector.<PageComponent>( _info.pages );

                loadPage( 0 , function( page : PageComponent ) : void {
                    if ( _info.pages > 1 && !_pageHeadHelper.isSingleHead( 0 ) ) {
                        addPage( page , true , function( page_ : PageComponent ) : void {
                            loadPage( 1 , function( page__ : PageComponent ) : void {
                                addPage( page__ , false , initLoadComplete );
                            } );
                        } );
                    } else {
                        addPage( page , false , function( page_ : PageComponent ) : void {
                            if ( _info.pages > 1 ) {
                                loadPage( 1 );
                            }

                            initLoadComplete( page_ );
                        } );
                    }
                } );

                loadPage( 2 );
                loadPage( 3 );
            } );
        }

        public function set mouseModeHandler( value : Function ) : * {
            _mouseActionHelper.mouseModeHandler = value;
        }

        public function removeHighlight() : void {
            getCurrentForePage().removeHighlight();
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

        private function addPage( page : PageComponent , isFore : Boolean , next : Function = null , hidden : Boolean =
            false ) : void {
            page.addEventListener( Event.ADDED_TO_STAGE , function() : void {
                page.removeEventListener( Event.ADDED_TO_STAGE , arguments.callee );

                page.callLater( function() : void {
                    // for layouting, maybe...
                    if ( isFore ) {
                        page.x = page.contentWidth;
                    } else {
                        page.x = 0;
                    }

                    if ( next != null ) {
                        next( page );
                    }
                } );
            } );

            // for cenering
            if ( isFore ) {
                page.x = page.contentWidth;
            } else {
                page.x = 0;
            }

            if ( hidden ) {
                _ui.wrapper.addChildAt( page , 0 );
            } else {
                _ui.wrapper.addChild( page );
            }
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

        private function calcBezierPoint( p0 : Point , p1 : Point , cp : Point , t : Number ) : Point {
            var v : Number = 1.0 - t;
            return new Point( v * v * p0.x + 2 * v * t * cp.x + t * t * p1.x ,
                              v * v * p0.y + 2 * v * t * cp.y + t * t * p1.y );
        }

        private function calcCenterX( contentWidth : Number , limit : Number = 0 ) : Number {
            return Math.max( ( _ui.scroller.width - contentWidth ) / 2 , limit );
        }

        private function calcCenterY( contentHeight : Number , limit : Number = 0 ) : Number {
            return Math.max( ( _ui.scroller.height - contentHeight ) / 2 , limit );
        }

        private function centering() : void {
            _ui.wrapper.x = calcCenterX( _ui.wrapper.width );
            _ui.wrapper.y = calcCenterY( _ui.wrapper.height );
        }

        private function changePage( head : int ) : void {
            if ( !_pageHeadHelper.isValidHead( head ) || head == _currentHead || _isAnimating ) {
                return;
            }

            // TODO
            /*
               if ( !_pages[ head ] ) {
               return;
               }
             */

            var current : Boolean = _pageHeadHelper.isSingleHead( _currentHead );
            var next : Boolean = _pageHeadHelper.isSingleHead( head );

            if ( current ) {
                if ( next ) {
                    changePage1to1( head );
                } else {
                    changePage1to2( head );
                }
            } else {
                if ( next ) {
                    changePage2to1( head );
                } else {
                    changePage2to2( head );
                }
            }
        }

        private function changePage1to1( head : int ) : void {
        }

        private function changePage1to2( head : int ) : void {
            var isForward : Boolean = head > _currentHead;

            var prev : PageComponent = getCurrentForePage();

            _currentHead = head;
            var nextFore : PageComponent = getCurrentForePage();
            var nextRear : PageComponent = getCurrentRearPage();

            if ( !isForward ) {
                _ui.wrapper.x -= _ui.wrapper.width;
                prev.x += prev.contentWidth;
            }

            var deltaX : Number =
                isForward ? calcCenterX( _ui.wrapper.width * 2 ) - calcCenterX( _ui.wrapper.width ) : ( calcCenterX( _ui.wrapper.width ) - calcCenterX( _ui.wrapper.width * 2 ) );

            var addFirst : PageComponent = isForward ? nextRear : nextFore;
            var addSecond : PageComponent = isForward ? nextFore : nextRear;

            addPage( addFirst , !isForward , function( page : PageComponent ) : void {
                addPage( addSecond , !isForward , function( page : PageComponent ) : void {
                    var front : PageComponent = isForward ? nextFore : prev;
                    var back : PageComponent = isForward ? prev : nextRear;

                    isAnimating = true;
                    startFlip( front , back , isForward , function() : void {
                        if ( _ui.wrapper.contains( prev ) ) {
                            _ui.wrapper.removeChild( prev );
                        }
                    } , function() : void {
                        if ( isForward ) {
                            nextFore.x = nextFore.contentWidth;
                        } else {
                            nextRear.x = 0;
                        }

                        loadNeighborPage();

                        isAnimating = false;
                    } , deltaX );
                } , true );
            } , true );
        }

        private function changePage2to1( head : int ) : void {
            var isForward : Boolean = head > _currentHead;

            var prevFore : PageComponent = getCurrentForePage();
            var prevRear : PageComponent = getCurrentRearPage();

            _currentHead = head;
            var next : PageComponent = getCurrentForePage();

            var deltaX : Number =
                calcCenterX( _ui.wrapper.width * ( isForward ? 3 : 1 ) / 2 ,
                             Number.NEGATIVE_INFINITY ) - calcCenterX( _ui.wrapper.width );

            addPage( next , isForward , function( page : PageComponent ) : void {
                var front : PageComponent = isForward ? next : prevFore;
                var back : PageComponent = isForward ? prevRear : next;
                var removeOnBegin : PageComponent = isForward ? prevRear : prevFore;
                var removeOnEnd : PageComponent = isForward ? prevFore : prevRear;

                isAnimating = true;
                startFlip( front , back , isForward , function() : void {
                    if ( _ui.wrapper.contains( removeOnBegin ) ) {
                        _ui.wrapper.removeChild( removeOnBegin );
                    }
                } , function() : void {
                    if ( _ui.wrapper.contains( removeOnEnd ) ) {
                        _ui.wrapper.removeChild( removeOnEnd );
                    }

                    if ( isForward ) {
                        next.x = 0;
                        _ui.wrapper.callLater( function() : void {
                            centering();
                        } );
                    }

                    loadNeighborPage();

                    isAnimating = false;
                } , deltaX );
            } , true );
        }

        private function changePage2to2( head : int ) : void {
            var isForward : Boolean = head > _currentHead;

            var prevFore : PageComponent = getCurrentForePage();
            var prevRear : PageComponent = getCurrentRearPage();

            _currentHead = head;
            var nextFore : PageComponent = getCurrentForePage();
            var nextRear : PageComponent = getCurrentRearPage();

            addPage( nextFore , true , function( page : PageComponent ) : void {
                addPage( nextRear , false , function( page : PageComponent ) : void {
                    var front : PageComponent = isForward ? nextFore : prevFore;
                    var back : PageComponent = isForward ? prevRear : nextRear;
                    var removeOnBegin : PageComponent = isForward ? prevRear : prevFore;
                    var removeOnEnd : PageComponent = isForward ? prevFore : prevRear;

                    isAnimating = true;
                    startFlip( front , back , isForward , function() : void {
                        if ( _ui.wrapper.contains( removeOnBegin ) ) {
                            _ui.wrapper.removeChild( removeOnBegin );
                        }
                    } , function() : void {
                        if ( _ui.wrapper.contains( removeOnEnd ) ) {
                            _ui.wrapper.removeChild( removeOnEnd );
                        }

                        /*nextFore.initSelection();
                           _isSelectingHandler( false );
                           _isSelectHighlightHandler( false );

                           if ( nextFore.hasRegions() ) {
                           nextFore.clearEmphasize();
                           } else {
                           loadRegions();
                         }*/

                        loadNeighborPage();

                        isAnimating = false;
                    } );
                } , true );
            } , true );
        }

        private function changeScale() : void {
            var hPos : Number =
                _ui.scroller.maxHorizontalScrollPosition > 0 ? ( _ui.scroller.horizontalScrollPosition ) / _ui.scroller.maxHorizontalScrollPosition : 0.5;
            var vPos : Number =
                _ui.scroller.maxVerticalScrollPosition > 0 ? ( _ui.scroller.verticalScrollPosition ) / _ui.scroller.maxVerticalScrollPosition : 0.5;

            _ui.wrapper.scaleX = _ui.wrapper.scaleY = _baseScale * Math.pow( 2 , _zoomExponent / 3.0 );

            getCurrentForePage().scale = _ui.wrapper.scaleX;

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
            _ui.arrowIndicator.isAnimatingFunc = isAnimatingFunc;

            new ResizeHelper( this , resizeHandler );

            _mouseActionHelper = new MouseActionHelper( _ui.scroller , systemManager , getCurrentForePage );
        }

        private function set currentPos( value : int ) : * {
            _currentHead = value;
            _setPageHandler( _currentHead , _info.pages );
        }

        private function easeInOutCubic( t : Number ) : Number {
            return t < 0.5 ? 4 * t * t * t : 4 * ( t - 1 ) * ( t - 1 ) * ( t - 1 ) + 1;
        }

        private function easeInOutQuart( t : Number ) : Number {
            return t < 0.5 ? 8 * t * t * t * t : -8 * ( t - 1 ) * ( t - 1 ) * ( t - 1 ) * ( t - 1 ) + 1;
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

        private function getCurrentForePage() : PageComponent {
            return _pages[ _pageHeadHelper.headToPage( _currentHead ) ];
        }

        private function getCurrentRearPage() : PageComponent {
            if ( _pageHeadHelper.isSingleHead( _currentHead ) ) {
                return null;
            }

            return _pages[ _pageHeadHelper.headToPage( _currentHead ) + 1 ];
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
            return _pageHeadHelper.isValidHead( _currentHead + 1 );
        }

        private function hasPrev() : Boolean {
            return _pageHeadHelper.isValidHead( _currentHead - 1 );
        }

        private function hasRight() : Boolean {
            return hasPrev();
        }

        private function initLoadComplete( page : PageComponent ) : void {
            currentPos = 0;
            _zoomExponent = 0;
            _isSelectingHandler( false );
            _isSelectHighlightHandler( false );

            fitWrapperSize( page );

            loadRegions();

        /*var leaf : Leaflip = new Leaflip( 480 , 640 );
           leaf.addFSP( new SAMPLE0() );
           leaf.addBSP( new SAMPLE1() );

           var constrain : Vector.<Point> = new Vector.<Point>();
           constrain.push( new Point( 480 - 1 , 1 ) , new Point( 480 - 1 , 640 - 1 ) );
           //var constrain : Array = [ new Point( 10 , 10 ) ];

           leaf.setConstrain( constrain );

           var ui : UIComponent = new UIComponent();
           //ui.x = ui.y = 200;
           //ui.width = ui.height = 200;
           ui.addChild( leaf );

         _ui.wrapper.addChild( ui );*/
        }

        private function isAnimatingFunc() : Boolean {
            return _isAnimating;
        }

        private function loadNeighborPage() : void {
            var page : int = _pageHeadHelper.headToPage( _currentHead );

            for ( var index : int = 0 ; index < _info.pages ; index++ ) {
                if ( index < page - 2 || index > page + 3 ) {
                    delete _pages[ index ];
                }
            }

            loadPage( page + 2 );
            loadPage( page + 3 );
            loadPage( page - 1 );
            loadPage( page - 2 );
        }

        private function loadPage( index : int , next : Function = null ) : void {
            if ( index >= 0 && index < _info.pages && !_pages[ index ] ) {
                DocumentLoadUtil.loadPage( _dto.id , index , _info.ratio , _pages , _isSelectHighlightHandler ,
                                           _initHighlightCommentHandler , _mouseActionHelper.mouseDownHandler ,
                                           changePage , next );
            }
        }

        private function loadRegions() : void {
            DocumentLoadUtil.loadRegions( _dto.id , _currentHead , getCurrentForePage() );
        }

        private function moveLeft() : void {
            moveToNextPage();
        }

        private function moveRight() : void {
            moveToPrevPage();
        }

        private function moveToNextPage() : void {
            changePage( _currentHead + 1 );
        }

        private function moveToPrevPage() : void {
            changePage( _currentHead - 1 );
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
            if ( getCurrentForePage() ) {
                fitWrapperSize( getCurrentForePage() );
            }
        }

        private function startFlip( current : PageComponent , next : PageComponent , isForward : Boolean ,
                                    beginFlip : Function , endFlip : Function , deltaX : Number = 0 ) : void {
            var MAX_STEP : int = 30;

            var w : Number = getCurrentForePage().width;
            var h : Number = getCurrentForePage().height;

            var render : Shape = new Shape();
            var wrapper : UIComponent = new UIComponent();
            wrapper.x = w;
            wrapper.addChild( render );
            _ui.wrapper.addChild( wrapper );

            var initX : Number = _ui.wrapper.x;

            var step : int = 0;
            systemManager.addEventListener( Event.ENTER_FRAME , function( e : Event ) : void {
                if ( step < MAX_STEP ) {
                    var sign : int = isForward ? 1 : -1;

                    render.graphics.clear();
                    var t : Number = easeInOutQuart( step / ( MAX_STEP - 1 ) );
                    var point : Point =
                        calcBezierPoint( new Point( -w * sign , h ) , new Point( w * sign , h ) ,
                                                    new Point( w / 2 * sign , 0 ) , t );
                    var o : Object = PageFlip.computeFlip( point , new Point( 1 , 1 ) , w , h , true , 1 );
                    PageFlip.drawBitmapSheet( o , render , current.bitmapData , next.bitmapData );

                    _ui.wrapper.x = initX + t * deltaX;

                    if ( step == 0 ) {
                        beginFlip();
                    }

                    step++;
                } else {
                    systemManager.removeEventListener( Event.ENTER_FRAME , arguments.callee );

                    _ui.wrapper.removeChild( wrapper );

                    endFlip();
                }
            } );
        }
    }
}
