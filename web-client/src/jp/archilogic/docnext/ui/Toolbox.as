package jp.archilogic.docnext.ui {
    import flash.events.Event;
    import flash.events.MouseEvent;
    import mx.containers.Canvas;
    import mx.events.FlexEvent;
    import jp.archilogic.docnext.type.MouseMode;

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
        private var _copyHandler : Function;
        private var _changeToHighlightHandler : Function;
        private var _changeHighlightColorHandler : Function;
        private var _removeHighlightHandler : Function;
        private var _changeHighlightCommentHandler : Function;

        public function set changeHighlightCommentHandler( value : Function ) : * {
            _changeHighlightCommentHandler = value;
        }

        public function set changeToHighlightHandler( value : Function ) : * {
            _changeToHighlightHandler = value;
        }

        public function set chnageHighlightColorHandler( value : Function ) : * {
            _changeHighlightColorHandler = value;
        }

        public function set copyHandler( value : Function ) : * {
            _copyHandler = value;
        }

        public function initHighlightComment( comment : String ) : void {
            _ui.highlightCommentTextInput.text = comment;
        }

        public function isSelectHighlightHandler( value : Boolean ) : void {
            _ui.redHighlightButton.enabled =
                _ui.greenHighlightButton.enabled =
                _ui.blueHighlightButton.enabled =
                _ui.removeHighlightButton.enabled = _ui.highlightCommentTextInput.enabled = value;
        }

        public function isSelectingHandler( value : Boolean ) : void {
            _ui.copyButton.enabled = _ui.changeToHighlightButton.enabled = value;
        }

        public function mouseModeHander() : MouseMode {
            switch ( _ui.mouseModeToggleButtonBar.selectedIndex ) {
                case 0:
                    return MouseMode.SCROLL;
                case 1:
                    return MouseMode.SELECT;
                default:
                    throw new Error();
            }
        }

        public function set removeHighlightHandler( value : Function ) : * {
            _removeHighlightHandler = value;
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

        private function blueHighlightButtonClickHandler( e : MouseEvent ) : void {
            _changeHighlightColorHandler( 0x0000ff );
        }

        private function changeToHighlightButtonClickHandler( e : MouseEvent ) : void {
            _changeToHighlightHandler();
        }

        private function copyButtonClickHanlder( e : MouseEvent ) : void {
            _copyHandler();
        }

        private function creationCompleteHandler( e : FlexEvent ) : void {
            _ui.removeEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );

            _ui.zoomInButton.addEventListener( MouseEvent.CLICK , zoomInButtonClickHandler );
            _ui.zoomOutButton.addEventListener( MouseEvent.CLICK , zoomOutButtonClickHandler );
            _ui.copyButton.addEventListener( MouseEvent.CLICK , copyButtonClickHanlder );
            _ui.changeToHighlightButton.addEventListener( MouseEvent.CLICK , changeToHighlightButtonClickHandler );
            _ui.redHighlightButton.addEventListener( MouseEvent.CLICK , redHighlightButtonClickHandler );
            _ui.greenHighlightButton.addEventListener( MouseEvent.CLICK , greenHighlightButtonClickHandler );
            _ui.blueHighlightButton.addEventListener( MouseEvent.CLICK , blueHighlightButtonClickHandler );
            _ui.removeHighlightButton.addEventListener( MouseEvent.CLICK , removeHighlightButtonClickHandler );
            _ui.highlightCommentTextInput.addEventListener( Event.CHANGE , highlightCommentTextInputChangeHandler );
        }

        private function greenHighlightButtonClickHandler( e : MouseEvent ) : void {
            _changeHighlightColorHandler( 0x00ff00 );
        }

        private function highlightCommentTextInputChangeHandler( e : Event ) : void {
            _changeHighlightCommentHandler( _ui.highlightCommentTextInput.text );
        }

        private function redHighlightButtonClickHandler( e : MouseEvent ) : void {
            _changeHighlightColorHandler( 0xff0000 );
        }

        private function removeHighlightButtonClickHandler( e : MouseEvent ) : void {
            _removeHighlightHandler();
        }

        private function zoomInButtonClickHandler( e : MouseEvent ) : void {
            _zoomInHandler();
        }

        private function zoomOutButtonClickHandler( e : MouseEvent ) : void {
            _zoomOutHandler();
        }
    }
}
