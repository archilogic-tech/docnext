package jp.archilogic.docnext.ui {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Loader;
    import flash.events.Event;
    import flash.geom.Matrix;
    import flash.geom.Point;
    import flash.utils.ByteArray;
    import mx.collections.ArrayCollection;
    import mx.controls.Image;
    import jp.archilogic.docnext.helper.OverlayAnnotationHelper;
    import jp.archilogic.docnext.helper.OverlayHelper;

    public class PageComponent extends Image {
        public function PageComponent() {
            super();

            _overlayHelper = new OverlayHelper( this );
        }

        private var _bd : BitmapData;
        private var _loaded : int;

        private var _overlayHelper : OverlayHelper;

        public function set annotation( value : Array ) : * {
            _overlayHelper.annotation = value;
        }

        public function changeHighlightColor( color : uint ) : void {
            _overlayHelper.changeHighlightColor( color );
        }

        public function changeHighlightComment( comment : String ) : void {
            _overlayHelper.changeHighlightComment( comment );
        }

        public function set changePageHandler( value : Function ) : * {
            _overlayHelper.changePageHandler = value;
        }

        public function changeSelectionToHighlight() : void {
            _overlayHelper.changeSelectionToHighlight();
        }

        public function clearEmphasize() : void {
            _overlayHelper.clearEmphasize();
        }

        public function set docId( value : Number ) : * {
            _overlayHelper.docId = value;
        }

        public function getNearTextPos( point : Point ) : int {
            return _overlayHelper.getNearTextPos( point );
        }

        public function hasRegions() : Boolean {
            return _overlayHelper.hasRegions();
        }

        public function hasSelectedText() : Boolean {
            return _overlayHelper.hasSelectedText();
        }

        public function set initHighlightCommentHandler( value : Function ) : * {
            _overlayHelper.initHighlightCommentHandler = value;
        }

        public function initSelection() : void {
            _overlayHelper.initSelection();
        }

        public function set isSelectHighlightHandler( value : Function ) : * {
            _overlayHelper.isSelectHighlightHandler = value;
        }

        public function loadData( data : ArrayCollection /* of ByteArray */ ) : void {
            _bd = null;
            _loaded = 0;

            loadHelper( ByteArray( data.getItemAt( 0 ) ) , 0 , 0 );
            loadHelper( ByteArray( data.getItemAt( 1 ) ) , 0 , 1 );
            loadHelper( ByteArray( data.getItemAt( 2 ) ) , 1 , 0 );
            loadHelper( ByteArray( data.getItemAt( 3 ) ) , 1 , 1 );
        }

        public function set page( value : int ) : * {
            _overlayHelper.page = value;
        }

        public function set ratio( value : Number ) : * {
            _overlayHelper.ratio = value;
        }

        public function set regions( value : Array ) : * {
            _overlayHelper.regions = value;
        }

        public function removeHighlight() : void {
            _overlayHelper.removeHighlight();
        }

        public function set scale( value : Number ) : * {
            _overlayHelper.scale = value;
        }

        public function get selectedText() : String {
            return _overlayHelper.selectedText;
        }

        public function showSelection( begin : int , end : int ) : void {
            _overlayHelper.showSelection( begin , end );
        }

        public function set text( value : String ) : * {
            _overlayHelper.text = value;
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
    }
}
