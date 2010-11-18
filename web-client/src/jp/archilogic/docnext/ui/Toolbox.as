package jp.archilogic.docnext.ui {
    import flash.events.Event;
    import flash.events.MouseEvent;
    import mx.containers.Canvas;
    import mx.controls.Alert;
    import mx.events.FlexEvent;

    public class Toolbox extends Canvas {
        public function Toolbox() {
            super();

            _ui = new ToolboxUI();
            _ui.addEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );
            addChild( _ui );
        }

        private var _ui : ToolboxUI;

        private var _zoomInHandler : Function;
        private var _zoomOutHandler : Function;
        private var _changeMenuVisibilityHandler : Function;
        private var _selectingHandler : Function;

        public function set changeMenuVisiblityHandler( value : Function ) : * {
            _changeMenuVisibilityHandler = value;
        }

        public function set selectingHandler( value : Function ) : * {
            _selectingHandler = value;
        }

        public function setPage( current : int , total : int ) : void {
            // change to 1-origin
            _ui.pageLabel.text = ( current + 1 ) + '/' + total;
        }

        public function set zoomInHandler( value : Function ) : * {
            _zoomInHandler = value;
        }

        public function set zoomOutHandler( value : Function ) : * {
            _zoomOutHandler = value;
        }

        private function alignComponentSize() : void {
            var maxWidth : Number =
                Math.max( _ui.textButton.width , _ui.tocButton.width , _ui.thumbnailButton.width ,
                          _ui.bookmarkButton.width , _ui.searchButton.width );

            _ui.textButton.width = maxWidth;
            _ui.tocButton.width = maxWidth;
            _ui.thumbnailButton.width = maxWidth;
            _ui.bookmarkButton.width = maxWidth;
            _ui.searchButton.width = maxWidth;
        }

        private function beginSelectionButtonClickHandler( e : MouseEvent ) : void {
            _selectingHandler( true );
            _changeMenuVisibilityHandler( false );
        }

        private function creationCompleteHandler( e : FlexEvent ) : void {
            _ui.removeEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );

            _ui.textButton.addEventListener( MouseEvent.CLICK , temp );
            _ui.tocButton.addEventListener( MouseEvent.CLICK , temp );
            _ui.thumbnailButton.addEventListener( MouseEvent.CLICK , temp );
            _ui.bookmarkButton.addEventListener( MouseEvent.CLICK , temp );
            _ui.searchButton.addEventListener( MouseEvent.CLICK , temp );
            _ui.beginSelectionButton.addEventListener( MouseEvent.CLICK , beginSelectionButtonClickHandler );

            _ui.zoomInButton.addEventListener( MouseEvent.CLICK , zoomInButtonClickHandler );
            _ui.zoomOutButton.addEventListener( MouseEvent.CLICK , zoomOutButtonClickHandler );

            alignComponentSize();
        }

        private function temp( e : MouseEvent ) : void {
            Alert.show( 'Under construction' );
        }

        private function zoomInButtonClickHandler( e : MouseEvent ) : void {
            _zoomInHandler();
        }

        private function zoomOutButtonClickHandler( e : MouseEvent ) : void {
            _zoomOutHandler();
        }
    }
}
