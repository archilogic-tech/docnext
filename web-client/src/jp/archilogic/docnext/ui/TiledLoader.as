package jp.archilogic.docnext.ui {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Loader;
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.geom.Matrix;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.net.SharedObject;
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;
    import mx.collections.ArrayCollection;
    import mx.containers.Canvas;
    import mx.controls.Image;
    import mx.utils.ObjectUtil;

    public class TiledLoader extends Image {
        private static const ALPHA_NORMAL : Number = 0.3;
        private static const ALPHA_EMPHASIZE : Number = 0.8;
        private static const KEY_TOP : String = 'top';
        private static const KEY_BOTTOM : String = 'bottom';
        private static const KEY_LEFT : String = 'left';
        private static const KEY_RIGHT : String = 'right';

        public function TiledLoader() {
            super();
        }

        private var _docId : Number;
        private var _page : int;

        private var _bd : BitmapData;
        private var _loaded : int;

        private var _isSelectHighlightHandler : Function;
        private var _initHighlightCommentHandler : Function;

        private var _regions : Array = null;
        private var _ratio : Number;
        private var _text : String;

        private var _currentSelections : Dictionary /* of Indicator */;
        private var _currentSelectionBegin : int;
        private var _currentSelectionEnd : int;

        private var _highlightInfos : Array /* of Object{begin,end,color,comment} */ = [];
        private var _currentHighlightIndex : int = -1;

        private var _highlights : Dictionary /* of <int,Dictionary<int,Indicator>> */ = new Dictionary();

        private var _balloons : Dictionary /* of <int,Balloon> */ = new Dictionary();

        private var _scale : Number;

        private var _foo : Number = 0;
        private var _bar : Number = 0;

        public function changeHighlightColor( color : uint ) : void {
            _highlightInfos[ _currentHighlightIndex ].color = color;

            saveState();

            for each ( var indicator : Canvas in _highlights[ _currentHighlightIndex ] ) {
                indicator.setStyle( 'backgroundColor' , color );
            }
        }

        public function changeHighlightComment( comment : String ) : void {
            _highlightInfos[ _currentHighlightIndex ].text = comment;

            saveState();

            if ( _balloons[ _currentHighlightIndex ] ) {
                removeChild( _balloons[ _currentHighlightIndex ] );
                delete _balloons[ _currentHighlightIndex ];
            }

            if ( comment.length > 0 ) {
                addBalloon( comment , _currentHighlightIndex );
            }
        }

        public function changeSelectionToHighlight() : void {
            var info : Object =
                { begin: _currentSelectionBegin , end: _currentSelectionEnd , color: 0x0000ff , text: '' };

            _highlightInfos.push( info );

            saveState();

            addHighlight( info , _highlightInfos.length - 1 );

            initSelection();
        }

        public function clearEmphasize() : void {
            emphasizeHighlight( -1 );
        }

        public function set docId( value : Number ) : * {
            _docId = value;
        }

        public function getNearTextPos( point : Point ) : int {
            var t : Number = new Date().time;

            var minDist : Number = Number.MAX_VALUE;
            var minIndex : int = -1;

            for ( var index : int = 0 ; index < _regions.length ; index++ ) {
                var rect : Rectangle = _regions[ index ];

                var dist : Number =
                    Math.pow( point.x - ( rect.x + rect.width / 2 ) ,
                              2 ) + Math.pow( point.y - ( rect.y + rect.height / 2 ) , 2 );

                if ( dist < minDist ) {
                    minDist = dist;
                    minIndex = index;
                }
            }

            _foo += new Date().time - t;

            return minIndex;
        }

        public function hasRegions() : Boolean {
            return _regions != null;
        }

        public function hasSelectedText() : Boolean {
            return _currentSelectionBegin != -1 && _currentSelectionEnd != -1;
        }

        public function set initHighlightCommentHandler( value : Function ) : * {
            _initHighlightCommentHandler = value;
        }

        public function initSelection() : void {
            for each ( var indicator : Canvas in _currentSelections ) {
                removeChild( indicator );
            }
            _currentSelections = null;

            _currentSelectionBegin = -1;
            _currentSelectionEnd = -1;
        }

        public function set isSelectHighlightHandler( value : Function ) : * {
            _isSelectHighlightHandler = value;
        }

        public function loadData( data : ArrayCollection /* of ByteArray */ ) : void {
            _bd = null;
            _loaded = 0;

            loadHelper( ByteArray( data.getItemAt( 0 ) ) , 0 , 0 );
            loadHelper( ByteArray( data.getItemAt( 1 ) ) , 0 , 1 );
            loadHelper( ByteArray( data.getItemAt( 2 ) ) , 1 , 0 );
            loadHelper( ByteArray( data.getItemAt( 3 ) ) , 1 , 1 );
        }

        public function loadState() : void {
            var so : SharedObject = SharedObject.getLocal( 'so' );

            if ( !so.data[ 'highlight' ] ) {
                so.data[ 'highlight' ] = {};
            }

            if ( !so.data[ 'highlight' ][ _docId ] ) {
                so.data[ 'highlight' ][ _docId ] = {};
            }

            if ( !so.data[ 'highlight' ][ _docId ][ _page ] ) {
                so.data[ 'highlight' ][ _docId ][ _page ] = [];
            }

            _highlightInfos = [];

            for each ( var info : Object in so.data[ 'highlight' ][ _docId ][ _page ] ) {
                _highlightInfos.push( info );
                addHighlight( info , _highlightInfos.length - 1 );
            }
        }

        public function set page( value : int ) : * {
            _page = value;
        }

        public function set ratio( value : Number ) : * {
            _ratio = value;
        }

        public function set regions( value : Array ) : * {
            _regions = [];

            for each ( var rect : Rectangle in value ) {
                var region : Rectangle = convertToStageRect( rect , new Point( width , height ) , _ratio );
                _regions.push( region );
            }
        }

        public function removeHighlight() : void {
            delete _highlightInfos[ _currentHighlightIndex ];

            saveState();

            for each ( var indicator : Canvas in _highlights[ _currentHighlightIndex ] ) {
                removeChild( indicator );
            }

            delete _highlights[ _currentHighlightIndex ];

            if ( _balloons[ _currentHighlightIndex ] ) {
                removeChild( _balloons[ _currentHighlightIndex ] );
                delete _balloons[ _currentHighlightIndex ];
            }

            _currentHighlightIndex = -1;

            _isSelectHighlightHandler( false );
        }

        public function saveState() : void {
            var so : SharedObject = SharedObject.getLocal( 'so' );

            // repack
            var data : Array = [];

            for each ( var info : Object in _highlightInfos ) {
                data.push( info );
            }

            so.data[ 'highlight' ][ _docId ][ _page ] = data;

            so.flush();
        }

        public function set scale( value : Number ) : * {
            _scale = value;

            for each ( var balloon : Balloon in _balloons ) {
                balloon.adjust( _scale );
            }
        }

        public function get selectedText() : String {
            return _text.substring( _currentSelectionBegin , _currentSelectionEnd );
        }

        public function showSelection( begin : int , end : int ) : void {
            var t : Number = new Date().time;

            if ( !hasSelectedText() ) {
                _currentSelections = new Dictionary();

                addSelection( begin , end );
            } else {
                if ( begin < _currentSelectionBegin ) {
                    addSelection( begin , _currentSelectionBegin );
                } else if ( begin > _currentSelectionBegin ) {
                    removeSelection( _currentSelectionBegin , begin );
                }

                if ( end > _currentSelectionEnd ) {
                    addSelection( _currentSelectionEnd , end );
                } else if ( end < _currentSelectionEnd ) {
                    removeSelection( end , _currentSelectionEnd );
                }
            }
            _currentSelectionBegin = begin;
            _currentSelectionEnd = end;

            _bar += new Date().time - t;

            trace( 'TiledLoader.Selection.Indicator.Prof.foo.bar' , _foo , _bar );
        }

        public function set text( value : String ) : * {
            _text = value;
        }

        private function addBalloon( comment : String , index : int ) : void {
            var met : Object = calcHighlightMetrics( index );
            var tip : Point = new Point( ( met[ KEY_LEFT ] + met[ KEY_RIGHT ] ) / 2.0 , met[ KEY_TOP ] );

            var balloon : Balloon = new Balloon( comment );
            balloon.parentTip = tip;
            balloon.adjust( _scale );

            addChild( balloon );

            _balloons[ index ] = balloon;
        }

        private function addHighlight( info : Object , index : int ) : void {
            _highlights[ index ] = new Dictionary();

            addOverlay( info.begin , info.end , info.color , _highlights[ index ] , function( e : MouseEvent ) : void {
                emphasizeHighlight( index );
                _currentHighlightIndex = index;

                _initHighlightCommentHandler( _balloons[ _currentHighlightIndex ] ? _balloons[ _currentHighlightIndex ].text : '' );

                _isSelectHighlightHandler( true );
            } );

            if ( info.text ) {
                addBalloon( info.text , index );
            }
        }

        private function addOverlay( begin : int , end : int , color : uint , holder : Dictionary = null ,
                                     clickHandler : Function = null ) : void {
            for ( var index : int = begin ; index < end ; index++ ) {
                var rect : Rectangle = _regions[ index ];

                var indicator : Canvas = new Canvas();
                indicator.x = rect.x;
                indicator.y = rect.y;
                indicator.width = rect.width;
                indicator.height = rect.height;
                indicator.setStyle( 'backgroundColor' , color );
                indicator.alpha = ALPHA_NORMAL;

                addChild( indicator );

                if ( holder != null ) {
                    holder[ index ] = indicator;
                }

                if ( clickHandler != null ) {
                    indicator.addEventListener( MouseEvent.CLICK , clickHandler );
                }
            }
        }

        private function addSelection( begin : int , end : int ) : void {
            addOverlay( begin , end , 0xff0000 , _currentSelections );
        }

        private function calcActualRect( size : Point , ratio : Number ) : Rectangle {
            var ret : Rectangle = new Rectangle( 0 , 0 , size.x , size.y );

            if ( ret.width < ret.height * ratio ) {
                // fit to width
                ret.y = ( ret.height - ret.width / ratio ) / 2.0;
                ret.height = ret.width / ratio;
            } else {
                // fit to height
                ret.x = ( ret.width - ret.height * ratio ) / 2.0;
                ret.width = ret.height * ratio;
            }

            return ret;
        }

        private function calcHighlightMetrics( index : int ) : Object {
            var ret : Object = {};
            ret[ KEY_TOP ] = Number.MAX_VALUE;
            ret[ KEY_BOTTOM ] = Number.MIN_VALUE;
            ret[ KEY_LEFT ] = Number.MAX_VALUE;
            ret[ KEY_RIGHT ] = Number.MIN_VALUE;

            for each ( var indicator : Canvas in _highlights[ index ] ) {
                ret[ KEY_TOP ] = Math.min( ret[ KEY_TOP ] , indicator.y );
                ret[ KEY_BOTTOM ] = Math.max( ret[ KEY_BOTTOM ] , indicator.y + indicator.height );
                ret[ KEY_LEFT ] = Math.min( ret[ KEY_LEFT ] , indicator.x );
                ret[ KEY_RIGHT ] = Math.max( ret[ KEY_RIGHT ] , indicator.x + indicator.width );
            }

            return ret;
        }

        private function convertToStageRect( rect : Rectangle , size : Point , ratio : Number ) : Rectangle {
            var actual : Rectangle = calcActualRect( size , ratio );

            return new Rectangle( actual.x + rect.x * actual.width , actual.y + rect.y * actual.height ,
                                  rect.width * actual.width , rect.height * actual.height );
        }

        private function emphasizeHighlight( targetIndex : int ) : void {
            for ( var key : String in _highlights ) {
                var index : int = parseInt( key );

                for each ( var indicator : Canvas in _highlights[ index ] ) {
                    indicator.alpha = index == targetIndex ? ALPHA_EMPHASIZE : ALPHA_NORMAL;
                }
            }
        }

        private function loadHelper( data : ByteArray , px : int , py : int ) : void {
            var loader : Loader = new Loader();

            loader.contentLoaderInfo.addEventListener( Event.COMPLETE , function() : void {
                loader.removeEventListener( Event.COMPLETE , arguments.callee );

                if ( _bd == null ) {
                    _bd = new BitmapData( loader.width * 2 , loader.height * 2 );
                }

                var mat : Matrix = new Matrix();
                mat.translate( loader.width * px , loader.height * py );
                _bd.draw( loader , mat );

                _loaded++;

                if ( _loaded == 4 ) {
                    source = new Bitmap( _bd , 'auto' , true );

                    dispatchEvent( new Event( Event.COMPLETE ) );
                }
            } );

            loader.loadBytes( data );
        }

        private function removeSelection( begin : int , end : int ) : void {
            for ( var index : int = begin ; index < end ; index++ ) {
                removeChild( _currentSelections[ index ] );
                delete _currentSelections[ index ];
            }
        }
    }
}
