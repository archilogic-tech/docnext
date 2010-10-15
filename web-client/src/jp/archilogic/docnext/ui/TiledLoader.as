package jp.archilogic.docnext.ui {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Loader;
    import flash.events.Event;
    import flash.geom.Matrix;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;
    import mx.collections.ArrayCollection;
    import mx.containers.Canvas;
    import mx.controls.Image;

    public class TiledLoader extends Image {
        public function TiledLoader() {
            super();
        }

        private var _bd : BitmapData;
        private var _loaded : int;

        private var _regions : Array = null;
        private var _ratio : Number;
        private var _text : String;

        private var _currentHighlights : Dictionary /* of Indicator */;
        private var _currentHighlightBegin : int;
        private var _currentHighlightEnd : int;

        private var _foo : Number = 0;
        private var _bar : Number = 0;

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
            return _currentHighlightBegin != -1 && _currentHighlightEnd != -1;
        }

        public function initHighlight() : void {
            for each ( var indicator : Canvas in _currentHighlights ) {
                removeChild( indicator );
            }
            _currentHighlights = null;

            _currentHighlightBegin = -1;
            _currentHighlightEnd = -1;
        }

        public function loadData( data : ArrayCollection /* of ByteArray */ ) : void {
            _bd = null;
            _loaded = 0;

            loadHelper( ByteArray( data.getItemAt( 0 ) ) , 0 , 0 );
            loadHelper( ByteArray( data.getItemAt( 1 ) ) , 0 , 1 );
            loadHelper( ByteArray( data.getItemAt( 2 ) ) , 1 , 0 );
            loadHelper( ByteArray( data.getItemAt( 3 ) ) , 1 , 1 );
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

        public function get selectedText() : String {
            return _text.substring( _currentHighlightBegin , _currentHighlightEnd );
        }

        public function showHighlight( begin : int , end : int ) : void {
            var t : Number = new Date().time;

            if ( !hasSelectedText() ) {
                _currentHighlights = new Dictionary();

                addHighlight( begin , end );
            } else {
                if ( begin < _currentHighlightBegin ) {
                    addHighlight( begin , _currentHighlightBegin );
                } else if ( begin > _currentHighlightBegin ) {
                    removeHighlight( _currentHighlightBegin , begin );
                }

                if ( end > _currentHighlightEnd ) {
                    addHighlight( _currentHighlightEnd , end );
                } else if ( end < _currentHighlightEnd ) {
                    removeHighlight( end , _currentHighlightEnd );
                }
            }
            _currentHighlightBegin = begin;
            _currentHighlightEnd = end;

            _bar += new Date().time - t;

            trace( _foo , _bar );
        }

        public function set text( value : String ) : * {
            _text = value;
        }

        private function addHighlight( begin : int , end : int ) : void {
            for ( var index : int = begin ; index < end ; index++ ) {
                var rect : Rectangle = _regions[ index ];

                var indicator : Canvas = new Canvas();
                indicator.x = rect.x;
                indicator.y = rect.y;
                indicator.width = rect.width;
                indicator.height = rect.height;
                indicator.setStyle( 'backgroundColor' , 0xff0000 );
                indicator.alpha = 0.3;

                _currentHighlights[ index ] = indicator;
                addChild( indicator );
            }
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

        private function convertToStageRect( rect : Rectangle , size : Point , ratio : Number ) : Rectangle {
            var actual : Rectangle = calcActualRect( size , ratio );

            return new Rectangle( actual.x + rect.x * actual.width , actual.y + rect.y * actual.height ,
                                  rect.width * actual.width , rect.height * actual.height );
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

        private function removeHighlight( begin : int , end : int ) : void {
            for ( var index : int = begin ; index < end ; index++ ) {
                removeChild( _currentHighlights[ index ] );
                delete _currentHighlights[ index ];
            }
        }
    }
}
