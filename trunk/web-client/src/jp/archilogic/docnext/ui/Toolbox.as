package jp.archilogic.docnext.ui {
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

        public function set copyHandler( value : Function ) : * {
            _copyHandler = value;
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

        public function set zoomInHandler( value : Function ) : * {
            _zoomInHandler = value;
        }

        public function set zoomOutHandler( value : Function ) : * {
            _zoomOutHandler = value;
        }

        private function copyButtonClickHanlder( e : MouseEvent ) : void {
            _copyHandler();
        }

        private function creationCompleteHandler( e : FlexEvent ) : void {
            _ui.removeEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );

            _ui.zoomInButton.addEventListener( MouseEvent.CLICK , zoomInButtonClickHandler );
            _ui.zoomOutButton.addEventListener( MouseEvent.CLICK , zoomOutButtonClickHandler );
            _ui.copyButton.addEventListener( MouseEvent.CLICK , copyButtonClickHanlder );
        }

        private function zoomInButtonClickHandler( e : MouseEvent ) : void {
            _zoomInHandler();
        }

        private function zoomOutButtonClickHandler( e : MouseEvent ) : void {
            _zoomOutHandler();
        }
    }
}
