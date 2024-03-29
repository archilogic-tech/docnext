package jp.archilogic.docnext.ui {
    import __AS3__.vec.Vector;
    
    import com.adobe.serialization.json.JSON;
    import com.foxaweb.pageflip.PageFlip;
    
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Shape;
    import flash.events.Event;
    import flash.events.KeyboardEvent;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    
    import jp.archilogic.docnext.dto.DocumentResDto;
    import jp.archilogic.docnext.helper.ContextMenuHelper;
    import jp.archilogic.docnext.helper.DocumentMouseEventHelper;
    import jp.archilogic.docnext.helper.PageHeadHelper;
    import jp.archilogic.docnext.helper.ResizeHelper;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.util.DocumentLoadUtil;
    
    import mx.collections.ArrayCollection;
    import mx.containers.Canvas;
    import mx.controls.Alert;
    import mx.controls.ProgressBar;
    import mx.controls.ProgressBarLabelPlacement;
    import mx.core.IIMESupport;
    import mx.core.UIComponent;
    import mx.events.FlexEvent;

    public class DocumentComponent extends Canvas {
        public function DocumentComponent() {
            _ui = new DocumentComponentUI();
            _ui.addEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );
            addChild( _ui );

            _contextMenuHelper = new ContextMenuHelper( this );

            _mouseEventHelper = new DocumentMouseEventHelper();
            _mouseEventHelper.contextMenuHelper = _contextMenuHelper;
            _mouseEventHelper.changePageFunc = changePageArrowClickHanlder;
            _mouseEventHelper.currentPagesFunc = getCurrentPages;

            addEventListener( Event.ADDED_TO_STAGE , addToStageHandler );
        }
       /*  public function get totalPage() : int {
        	return _dtos.length;
        } */
        public function get pages() : Vector.<PageComponent> {
        	return _pages[_currentDocPos];
        }
		public function getCurrentHead():int {
			return this._currentHead;
		}
		public function get currentDocPos():int {
			return this._currentDocPos;
		}
		public function get ratio() : int {
			return _infos[ _currentDocPos ].ratio;
		}
		public function get docId() : int {
			return _dtos[ _currentDocPos ].id;
		} 
		public function get background() : Bitmap {
			/* var bd : BitmapData = new BitmapData(_ui.wrapper.width, _ui.wrapper.height);
			bd.draw(this._ui.wrapper);
			var bitmap : Bitmap = new Bitmap(bd);
			bitmap.width = this.width;
			bitmap.height = this.height; */
			var bd : BitmapData = new BitmapData( width, height);
			bd.draw(this);
			var bitmap : Bitmap = new Bitmap(bd);
			bitmap.width = this.width;
			bitmap.height = this.height;
			return bitmap;
		}

        public function get flow() : Boolean {
            return "right" != _infos[ _currentDocPos ].flow;
        }
		
		/* public function get infos() : Vector.<Object> {
			return this._infos;
		} */
		/*  adhoc quick fix */
		/* private var initNeighborCount : int ; */
		public function setCurrentHead(index : int) : void
		{
			var total : int = _pages[_currentDocPos].length ;
			 /* _currentHead = index * 0.5; */
			if ( index == ( total -1 ) ) index--; 
			currentHead = Math.floor(index * 0.5) ;
			
            for ( var i : int = 0 ; i < total ; i++ ) {
                 delete _pages[ _currentDocPos ][ i ];
                 _pages[ _currentDocPos ][ i ] = null;
            }
            _ui.wrapper.removeAllChildren();
            
            //this.changeHead(_currentHead);
           /*  changePageHandler(index); */
           
           _loadingCount = 0;
           /*  @ adhoc quick fix @
           
           		button should be invalidated while loading
           		if a user change page while loading,
           		page never change
           */
           /* initNeighborCount = 0;
           _mouseEventHelper.arrowIndicator.visible = false;
           var initFunc : Function = function( p : PageComponent) : void
           {
           		initNeighborCount++;
           		if(initNeighborCount >= 6)
           		{
           			_mouseEventHelper.arrowIndicator.visible = true;
           			Alert.show('init');
           		}
           			 
           } */
           var loadIndex : int = _pageHeadHelpers[ _currentDocPos ].headToPage( _currentHead );
            loadPage( loadIndex , function( page : PageComponent ) : void 
            {
            	/* initNeighborCount++; */
                if ( _infos[ _currentDocPos ].pages > 1 && !_pageHeadHelpers[_currentDocPos ].isSingleHead( _currentHead ) ) 
                {
                     addPage( page , true , function( page_ : PageComponent ) : void 
                    {
                        loadPage( loadIndex + 1 , function( page__ : PageComponent ) : void 
                        {
                        	/* initNeighborCount++; */
                            addPage( page__ , false, null, true );
                           /*  loadPage(loadIndex + 2 , initFunc);
                            loadPage(loadIndex + 3 , initFunc);
                            loadPage(loadIndex - 1 , initFunc);
                            loadPage(loadIndex - 2 , initFunc); */
                            loadPage(loadIndex + 2 );
                            loadPage(loadIndex + 3 );
                            loadPage(loadIndex - 1 );
                            loadPage(loadIndex - 2 );
                            loadPage(loadIndex + 4 );
                            loadPage(loadIndex + 5 );
                            loadPage(loadIndex - 3 );
                            loadPage(loadIndex - 4 );
                            loadPage(loadIndex + 6 );
                            loadPage(loadIndex + 7 );
                            loadPage(loadIndex - 5 );
                            loadPage(loadIndex - 6 );
                        } );
                    } , true); 
                }
                else
                {
                     addPage( page , false , function( page_ : PageComponent ) : void 
                    {
                        if ( _infos[ _currentDocPos ].pages > 1 ) 
                        {
                            loadPage( index + 1 );
                        }
                    } ); 
                }
            } );  
            
            
		}
		public function hasPage(currentDocPos: int , currentHead : int ) : Boolean
		{
			var value : Boolean = false;
			if(_pages[currentDocPos][currentHead] != null)
				value = true;
				
			return value;
		}
		public function removeEvents() : void
		{
			 stage.removeEventListener(KeyboardEvent.KEY_UP , myKeyUpHandler ); 
			 /* resizeHelper = new ResizeHelper(this, resizeHandler ); */
		}
		public function addEvents() : void
		{
			 stage.addEventListener( KeyboardEvent.KEY_UP , myKeyUpHandler ); 
			 /* resizeHelper.addResizeHelper(); */
			  resizeHandler(); 
		}
		/* private var resizeHelper : ResizeHelper; */
		private var _ui : DocumentComponentUI;
        private var _currentDocPos : int;
        private var _currentHead : int;
        private var _pages : Vector.<Vector.<PageComponent>>;
        private var _loadingCount : int;
        private var _dtos : Vector.<DocumentResDto>;
        private var _infos : Vector.<Object>;
        private var _pageHeadHelpers : Vector.<PageHeadHelper>;
        private var _baseScale : Number;
        private var _zoomExponent : int;
        private var _setPageHandler : Function;
        private var _isAnimating : Boolean;
        private var _mouseEventHelper : DocumentMouseEventHelper;
        private var _contextMenuHelper : ContextMenuHelper;

        public function set changeMenuVisiblityHandler( value : Function ) : * {
            _mouseEventHelper.changeMenuVisiblityFunc = value;
        }

        public function set isMenuVisibleHandler( value : Function ) : * {
            _mouseEventHelper.isMenuVisibleFunc = value;
        }

        public function load( dtos : Vector.<DocumentResDto> ) : void {
            _dtos = dtos;
            _infos = new Vector.<Object>( _dtos.length );
            _pageHeadHelpers = new Vector.<PageHeadHelper>( _dtos.length );
            _pages = new Vector.<Vector.<PageComponent>>( _dtos.length );
			
            loadHelepr( 0 );
        }

        public function set selecting( value : Boolean ) : void {
            _mouseEventHelper.selecting = value;
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

            // for centering
            if ( isFore ) {
                page.x = page.contentWidth;
            } else {
                page.x = 0;
            }

            if ( hidden ) {
            	/* _ui.wrapper.addChild( page ); */
                 _ui.wrapper.addChildAt( page , 0 ); 
            } else {
                _ui.wrapper.addChild( page );
            }
        }

        private function addToStageHandler( e : Event ) : void {
        	this.removeEventListener(Event.ADDED_TO_STAGE , addToStageHandler );
            stage.addEventListener( KeyboardEvent.KEY_UP , myKeyUpHandler );
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

        private function changeHead( head : int ) : void {
            var nextDocPos : int = _currentDocPos;

            if ( head < 0 && _currentDocPos - 1 >= 0 ) {
                nextDocPos--;
                head += _pageHeadHelpers[ nextDocPos ].length;
            } else if ( head >= _pageHeadHelpers[ _currentDocPos ].length && _currentDocPos + 1 < _infos.length ) {
                nextDocPos++;
                head -= _pageHeadHelpers[ _currentDocPos ].length;
            }

            if ( !_pageHeadHelpers[ nextDocPos ].isValidHead( head ) ||
                nextDocPos == _currentDocPos && head == _currentHead || _isAnimating || _loadingCount > 0 ) {
                return;
            }

            var current : Boolean = _pageHeadHelpers[ _currentDocPos ].isSingleHead( _currentHead );
            var next : Boolean = _pageHeadHelpers[ nextDocPos ].isSingleHead( head );

            if ( current ) {
                if ( next ) {
                    changePage1to1( head , nextDocPos );
                } else {
                    changePage1to2( head , nextDocPos );
                }
            } else {
                if ( next ) {
                    changePage2to1( head , nextDocPos );
                } else {
                    changePage2to2( head , nextDocPos );
                }
            }
        }

        private function changePage1to1( head : int , docPos : int ) : void {
            var prev : PageComponent = getCurrentForePage();

            var prevDocPos : int = _currentDocPos;
            var prevHead : int = _currentHead;

            _currentDocPos = docPos;
            currentHead = head;
            var next : PageComponent = getCurrentForePage();

            if ( !next ) {
                loadPage( _pageHeadHelpers[ docPos ].headToPage( head ) , function( page : PageComponent ) : void {
                    changePage1to1( head , docPos );
                } );

                _currentDocPos = prevDocPos;
                currentHead = prevHead;
                return;
            }

            next.scale = _ui.wrapper.scaleX;

            addPage( next , false , function( page : PageComponent ) : void {
                isAnimating = true;
                startFlip( null , prev , true , function() : void {
                    if ( _ui.wrapper.contains( prev ) ) {
                        _ui.wrapper.removeChild( prev );
                    }
                } , function() : void {
                    next.initSelection();
                    next.clearEmphasize();

                    changePageCleanUp();

                    isAnimating = false;
                } );
            } , true );
        }

        private function changePage1to2( head : int , docPos : int ) : void {
            var isForward : Boolean = docPos > _currentDocPos || docPos >= _currentDocPos && head > _currentHead;

            var prev : PageComponent = getCurrentForePage();

            var prevDocPos : int = _currentDocPos;
            var prevHead : int = _currentHead;

            _currentDocPos = docPos;
            currentHead = head;
            var nextFore : PageComponent = getCurrentForePage();
            var nextRear : PageComponent = getCurrentRearPage();

            if ( !nextFore ) {
                loadPage( _pageHeadHelpers[ docPos ].headToPage( head ) , function( page : PageComponent ) : void {
                    changePage1to2( head , docPos );
                } );

                _currentDocPos = prevDocPos;
                currentHead = prevHead;
                return;
            }

            if ( !nextRear ) {
                loadPage( _pageHeadHelpers[ docPos ].headToPage( head ) + 1 , function( page : PageComponent ) : void {
                    changePage1to2( head , docPos );
                } );

                _currentDocPos = prevDocPos;
                currentHead = prevHead;
                return;
            }

            nextFore.scale = nextRear.scale = _ui.wrapper.scaleX;

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

                        nextFore.initSelection();
                        nextFore.clearEmphasize();
                        nextRear.initSelection();
                        nextRear.clearEmphasize();

                        changePageCleanUp();

                        isAnimating = false;
                    } , deltaX );
                } , true );
            } , true );
        }

        private function changePage2to1( head : int , docPos : int ) : void {
            var isForward : Boolean = docPos > _currentDocPos || docPos >= _currentDocPos && head > _currentHead;

            var prevFore : PageComponent = getCurrentForePage();
            var prevRear : PageComponent = getCurrentRearPage();

            var prevDocPos : int = _currentDocPos;
            var prevHead : int = _currentHead;

            _currentDocPos = docPos;
            currentHead = head;
            var next : PageComponent = getCurrentForePage();

            if ( !next ) {
                loadPage( _pageHeadHelpers[ docPos ].headToPage( head ) , function( page : PageComponent ) : void {
                    changePage2to1( head , docPos );
                } );

                _currentDocPos = prevDocPos;
                currentHead = prevHead;
                return;
            }

            next.scale = _ui.wrapper.scaleX;

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

                    next.initSelection();
                    next.clearEmphasize();

                    changePageCleanUp();

                    isAnimating = false;
                } , deltaX );
            } , true );
        }

        private function changePage2to2( head : int , docPos : int ) : void {
            var isForward : Boolean = docPos > _currentDocPos || docPos >= _currentDocPos && head > _currentHead;

            var prevFore : PageComponent = getCurrentForePage();
            var prevRear : PageComponent = getCurrentRearPage();

            var prevDocPos : int = _currentDocPos;
            var prevHead : int = _currentHead;

            _currentDocPos = docPos;
            currentHead = head;
            var nextFore : PageComponent = getCurrentForePage();
            var nextRear : PageComponent = getCurrentRearPage();
			
            if ( !nextFore ) {
                loadPage( _pageHeadHelpers[ docPos ].headToPage( head ) , function( page : PageComponent ) : void {
                	
                    changePage2to2( head , docPos );
                } );
				
                _currentDocPos = prevDocPos;
                currentHead = prevHead;
                return;
            }

            if ( !nextRear ) {
                loadPage( _pageHeadHelpers[ docPos ].headToPage( head ) + 1 , function( page : PageComponent ) : void {
                    changePage2to2( head , docPos );
                } );

                _currentDocPos = prevDocPos;
                currentHead = prevHead;
                return;
            }
            nextFore.scale = nextRear.scale = _ui.wrapper.scaleX;

            addPage( nextFore , true , function( page : PageComponent ) : void {
                addPage( nextRear , false , function( page : PageComponent ) : void {
                    var front : PageComponent = isForward == flow ? nextFore : prevFore;
                    var back : PageComponent = isForward == flow ? prevRear : nextRear;
                    var removeOnBegin : PageComponent = isForward == flow ? prevRear : prevFore;
                    var removeOnEnd : PageComponent = isForward == flow ? prevFore : prevRear;

                    isAnimating = true;
                    if(isForward == flow) nextFore.visible = false;
                    else nextRear.visible = false;  
                    startFlip( front , back , isForward , 
                    function() : void 
                    {
                        if ( removeOnBegin != null && _ui.wrapper.contains( removeOnBegin ) ) {
                            _ui.wrapper.removeChild( removeOnBegin );
                        }
                    } , 
                    function() : void 
                    {
                    	nextFore.visible = true;
                    	nextRear.visible = true;  
                    	/* nextFore.alpha = nextRear.alpha = 1; */
                        if ( removeOnEnd != null && _ui.wrapper.contains( removeOnEnd ) ) 
                        {
                            _ui.wrapper.removeChild( removeOnEnd );
                        }

                        nextFore.initSelection();
                        nextFore.clearEmphasize();
                        nextRear.initSelection();
                        nextRear.clearEmphasize();

                        changePageCleanUp(); 

                        isAnimating = false;
                    } );
                } , false );
        } , false );
        
             /*    } , true );
            } , true ); */
        }

        private function changePageArrowClickHanlder( delta : int ) : void {
            if ( delta > 0 ) {
                moveLeft();
            } else {
                moveRight();
            }
        }

        private function changePageCleanUp() : void {
            loadNeighborPage();
        }

        private function changePageHandler( page : int ) : void {
            changeHead( _pageHeadHelpers[ _currentDocPos ].pageToHead( page ) );
        }

        private function changeScale() : void {
            var hPos : Number =
                _ui.scroller.maxHorizontalScrollPosition > 0 ? ( _ui.scroller.horizontalScrollPosition ) / _ui.scroller.maxHorizontalScrollPosition : 0.5;
            var vPos : Number =
                _ui.scroller.maxVerticalScrollPosition > 0 ? ( _ui.scroller.verticalScrollPosition ) / _ui.scroller.maxVerticalScrollPosition : 0.5;

            var scale : Number = _baseScale * Math.pow( 2 , _zoomExponent / 3.0 );

            _ui.wrapper.scaleX = _ui.wrapper.scaleY = scale;

            getCurrentForePage().scale = scale;

            if ( getCurrentRearPage() ) {
                getCurrentRearPage().scale = scale;
            }

            _ui.wrapper.callLater( function() : void {
                centering();

                _ui.scroller.horizontalScrollPosition = hPos * _ui.scroller.maxHorizontalScrollPosition;
                _ui.scroller.verticalScrollPosition = vPos * _ui.scroller.maxVerticalScrollPosition;
            } );
        }

        private function creationCompleteHandler( e : FlexEvent ) : void {
            _ui.removeEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );

            _ui.arrowIndicator.pageRectFunc = pageRectHandler;
            _ui.arrowIndicator.hasLeftFunc = hasLeft;
            _ui.arrowIndicator.hasRightFunc = hasRight;
            _ui.arrowIndicator.isAnimatingFunc = isAnimatingFunc;

            /* resizeHelper = new ResizeHelper( this , resizeHandler ); */
            new ResizeHelper( this , resizeHandler );
            
            var progress : ProgressBar = new ProgressBar();
            progress.indeterminate = true;
            progress.labelPlacement = ProgressBarLabelPlacement.CENTER;
            
            _ui.wrapper.addChild( progress );
            
            progress.x = ( _ui.width - 150 ) / 2;
            progress.y = ( _ui.height - 4 ) / 2;
        }
        private function set currentHead( value : int ) : * {
            _currentHead = value;

            var sumToCurrent : int = 0;
            var sumTotal : int = 0;

            for ( var index : int = 0 ; index < _infos.length ; index++ ) {
                if ( index < _currentDocPos ) {
                    sumToCurrent += _infos[ _currentDocPos ].pages;
                }

                sumTotal += _infos[ _currentDocPos ].pages;
            }

            _setPageHandler( _pageHeadHelpers[ _currentDocPos ].headToPage( _currentHead ) + sumToCurrent , sumTotal );
        }

        private function easeInOutQuart( t : Number ) : Number {
            // cubic
            // return t < 0.5 ? 4 * t * t * t : 4 * ( t - 1 ) * ( t - 1 ) * ( t - 1 ) + 1;

            return t < 0.5 ? 8 * t * t * t * t : -8 * ( t - 1 ) * ( t - 1 ) * ( t - 1 ) * ( t - 1 ) + 1;
        }

        private function fitWrapperSize( page : PageComponent ) : void {
        	if(page == null || _ui == null || page.content == null) return;
            if ( _ui.width / page.content.width > _ui.height / page.content.height ) {
                // fit to height
                _baseScale = _ui.height / page.content.height;
            } else {
                // fit to width
                _baseScale = _ui.width / page.content.width;
            }

            changeScale();
        }
		
        private function getCurrentForePage() : PageComponent {
            if ( !_pages ) {
                return null;
            }

            return _pages[ _currentDocPos ][ _pageHeadHelpers[ _currentDocPos ].headToPage( _currentHead ) ];
        }
		
        private function getCurrentPages() : Vector.<PageComponent> {
            var ret : Vector.<PageComponent> = new Vector.<PageComponent>();

            ret.push( getCurrentForePage() );

            if ( getCurrentRearPage() ) {
                ret.push( getCurrentRearPage() );
            }

            return ret;
        }
        
        private function getCurrentRearPage() : PageComponent {
            if ( !_pages || !_pages[ _currentDocPos ] ||
                _pageHeadHelpers[ _currentDocPos ].isSingleHead( _currentHead ) ) {
                return null;
            }

            return _pages[ _currentDocPos ][ _pageHeadHelpers[ _currentDocPos ].headToPage( _currentHead ) + 1 ];
        }

        private function hasLeft() : Boolean {
            return flow ? hasNext() : hasPrev();
        }

        private function hasNext() : Boolean {
            return _currentDocPos + 1 < _infos.length ||
                _pageHeadHelpers[ _currentDocPos ].isValidHead( _currentHead + 1 );
        }

        private function hasPrev() : Boolean {
            return _currentDocPos - 1 >= 0 || _pageHeadHelpers[ _currentDocPos ].isValidHead( _currentHead - 1 );
        }

        private function hasRight() : Boolean {
            return flow ? hasPrev() : hasNext();
        }

        private function initLoadComplete( page : PageComponent ) : void {
            currentHead = 0;
            _currentDocPos = 0;
            _zoomExponent = 0;

            fitWrapperSize( page );

            _mouseEventHelper.scroller = _ui.scroller;
            _mouseEventHelper.arrowIndicator = _ui.arrowIndicator;
            _mouseEventHelper.init( this );
        }

        private function set isAnimating( value : Boolean ) : * {
            _isAnimating = value;

            if ( _isAnimating ) {
                _ui.arrowIndicator.startAnimating();
            } else {
                _ui.arrowIndicator.endAnimating();
            }
        }

        private function isAnimatingFunc() : Boolean {
            return _isAnimating;
        }

        private function loadHelepr( position : int ) : void {
            if ( position < _dtos.length ) {
                DocumentService.getInfo( _dtos[ position ].id , function( json : String ) : void {
                    _infos[ position ] = JSON.decode( json );

                    DocumentService.getSinglePageInfo( _dtos[ position ].id ,
                                                       function( singlePages : ArrayCollection ) : void {
                        _pageHeadHelpers[ position ] =
                            new PageHeadHelper( singlePages.toArray() , _infos[ position ].pages );
                        _pages[ position ] = new Vector.<PageComponent>( _infos[ position ].pages );

                        loadHelepr( position + 1 );
                    } );
                } );
            } else {
                _loadingCount = 0;

                loadPage( 0 , function( page : PageComponent ) : void {
                    if ( _infos[ 0 ].pages > 1 && !_pageHeadHelpers[ 0 ].isSingleHead( 0 ) ) {
                        addPage( page , true , function( page_ : PageComponent ) : void {
                            loadPage( 1 , function( page__ : PageComponent ) : void {
                                addPage( page__ , false , initLoadComplete );
                            } );
                        } );
                    } else {
                        addPage( page , false , function( page_ : PageComponent ) : void {
                            if ( _infos[ 0 ].pages > 1 ) {
                                loadPage( 1 );
                            }

                            initLoadComplete( page_ );
                        } );
                    }
                } );

                loadPage( 2 );
                loadPage( 3 );
            }
        }

        /**
         * Ignore neighbor document, currently
         */
        private function loadNeighborPage() : void {
            var page : int = _pageHeadHelpers[ _currentDocPos ].headToPage( _currentHead );

            for ( var index : int = 0 ; index < _infos[ _currentDocPos ].pages ; index++ ) {
                if ( index < page - 2 || index > page + 7 ) {
                    delete _pages[ _currentDocPos ][ index ];
                    _pages[ _currentDocPos ][ index ] = null;
                }
            }

            loadPage( page + 2 );
            loadPage( page + 3 );
            loadPage( page - 1 );
            loadPage( page - 2 );
            loadPage( page + 4 );
            loadPage( page + 5 );
            loadPage( page - 3 );
            loadPage( page - 4 );
            loadPage( page + 6 );
            loadPage( page + 7 );
            loadPage( page - 5 );
            loadPage( page - 6 );
        }

        private function loadPage( index : int , next : Function = null ) : void {
            if ( index >= 0 && index < _infos[ _currentDocPos ].pages && !_pages[ _currentDocPos ][ index ] ) {
                _loadingCount++;

                DocumentLoadUtil.loadPage( _dtos[ _currentDocPos ].id , index , _infos[ _currentDocPos ].ratio ,
                                           _pages[ _currentDocPos ] , _ui.scroller , _contextMenuHelper ,
                                           _mouseEventHelper.isMenuVisbleFunc , changePageHandler ,
                                           function( page : PageComponent ) : void {
                    _loadingCount--;

                    if ( next != null ) {
                        next( page );
                    }
                } );
            }
        }
        
        public function loadForThumb(index : int , next : Function = null ) : void
        {
        	/*  _currentHead = Math.floor(index*0.5);   */
        	/*  changePageCleanUp();   */
        	var page : PageComponent = _pages[_currentDocPos][index];
			var total : int = _pages[_currentDocPos].length ;
            for ( var i : int = 0 ; i < total ; i++ ) {
                 delete _pages[ _currentDocPos ][ i ];
                 _pages[ _currentDocPos ][ i ] = null;
            }
            loadPage( index, next);
            /* loadPage( index + 1 );
            loadPage( index + 2 );
            loadPage( index - 1 ); */
        	
        	
        	/* loadPage(index+1, next); */
        }
		/*  public function hasCache(index : int ) : Boolean
		{
			var page : PageComponent = _pages[_currentDocPos ][index];
			if( page != null && page.source !=null && page.bitmapData != null)
				return true;
			return false;
		}  */
        private function moveLeft() : void {
            flow ? moveToNextPage() : moveToPrevPage();
        }

        private function moveRight() : void {
            flow ? moveToPrevPage() : moveToNextPage();
        }

        private function moveToNextPage() : void {
            changeHead( _currentHead + 1 );
        }

        private function moveToPrevPage() : void {
            changeHead( _currentHead - 1 );
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

        private function nullSafeGetBitmapData( page : PageComponent ) : BitmapData {
            if ( page ) {
                return page.bitmapData;
            } else {
                var current : PageComponent = getCurrentForePage();
                return new BitmapData( current.width , current.height , true , 0 );
            }
        }

        private function pageRectHandler() : Rectangle {
            return new Rectangle( _ui.wrapper.x , _ui.wrapper.y ,
                                  Math.min( _ui.scroller.width - _ui.wrapper.x , _ui.wrapper.width ) ,
                                  Math.min( _ui.scroller.height - _ui.wrapper.y , _ui.wrapper.height ) );
        }

        private function resizeHandler() : void {
        	if(this.visible == false ) return;
        	if(_ui == null ) return;
            if ( getCurrentForePage() ) {
                fitWrapperSize( getCurrentForePage() );
            }
        }

        private function startFlip( front : PageComponent , back : PageComponent , isForward : Boolean ,
                                    beginFlip : Function , endFlip : Function , deltaX : Number = 0 ) : void {
            var N_STEP : int = 12;

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
                if ( step < N_STEP ) {
                    var sign : int = isForward == flow ? 1 : -1;

                    render.graphics.clear();
                    var t : Number = easeInOutQuart( step / ( N_STEP - 1 ) );
                    var point : Point =
                        calcBezierPoint( new Point( -w * sign , h ) , new Point( w * sign , h ) ,
                                                    new Point( w / 2 * sign , 0 ) , t );
                    var o : Object = PageFlip.computeFlip( point , new Point( 1 , 1 ) , w , h , true , 1 );
                    PageFlip.drawBitmapSheet( o , render , nullSafeGetBitmapData( front ) ,
                                              nullSafeGetBitmapData( back ) );

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
