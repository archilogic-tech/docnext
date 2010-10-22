package jp.archilogic.docnext.ui {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Loader;
    import flash.events.Event;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.utils.ByteArray;
    import mx.controls.Image;
    import __AS3__.vec.Vector;
    import jp.archilogic.docnext.helper.OverlayHelper;

    public class PageComponent extends Image {
        public function PageComponent() {
            super();

            _overlayHelper = new OverlayHelper( this );
        }

        private var _overlayHelper : OverlayHelper;

        public function set annotation( value : Array ) : * {
            _overlayHelper.annotation = value;
        }

        public function get bitmapData() : BitmapData {
            return Bitmap( source ).bitmapData;
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

        public function get docId() : Number {
            return _overlayHelper.docId;
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

        public function loadData( data : ByteArray ) : void {
            var loader : Loader = new Loader();

            loader.contentLoaderInfo.addEventListener( Event.COMPLETE , function() : void {
                loader.removeEventListener( Event.COMPLETE , arguments.callee );

                var bd : BitmapData = new BitmapData( loader.width , loader.height );
                bd.draw( loader );

                source = new Bitmap( bd , 'auto' , true );

                width = loader.width;
                height = loader.height;

                dispatchEvent( new Event( Event.COMPLETE ) );
            } );

            loader.loadBytes( data );
        }

        public function get page() : int {
            return _overlayHelper.page;
        }

        public function set page( value : int ) : * {
            _overlayHelper.page = value;
        }

        public function set ratio( value : Number ) : * {
            _overlayHelper.ratio = value;
        }

        public function set regions( value : Vector.<Rectangle> ) : * {
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
    }
}
