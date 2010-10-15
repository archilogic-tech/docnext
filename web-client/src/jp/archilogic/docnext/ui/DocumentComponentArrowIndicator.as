package jp.archilogic.docnext.ui {
    import flash.display.StageDisplayState;
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import mx.containers.Canvas;
    import mx.controls.Image;
    import jp.archilogic.docnext.resource.Resource;

    public class DocumentComponentArrowIndicator extends Canvas {
        private static const ARROW_HORIZONTAL_SIZE : Object = { width: 76 , height: 87 };
        private static const ARROW_MODE_RIGHT : int = 1;
        private static const ARROW_MODE_LEFT : int = 0;
        private static const ARROW_MODE_UNDEFINED : int = -1;
        private static const ARROW_PADDING : Number = 10;
        private static const ARROW_THREASHOLD : Number = 100;

        public function DocumentComponentArrowIndicator() {
            super();

            addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
            addEventListener( MouseEvent.ROLL_OUT , arrowLayoutMouseOutHandler );
            addEventListener( MouseEvent.CLICK , mouseClickHandler );
        }

        private var _topPosFunc : Function;
        private var _bottomPosFunc : Function;
        private var _leftPosFunc : Function;
        private var _rightPosFunc : Function;
        private var _clickHandler : Function;
        private var _hasLeftFunc : Function;
        private var _hasRightFunc : Function;

        private var _currentArrow : Image;
        private var _arrowMode : int;

        public function set bottomPosFunc( value : Function ) : void {
            _bottomPosFunc = value;
        }

        public function set clickHandler( value : Function ) : void {
            _clickHandler = value;
        }

        public function set hasLeftFunc( value : Function ) : void {
            _hasLeftFunc = value;
        }

        public function set hasRightFunc( value : Function ) : void {
            _hasRightFunc = value;
        }

        public function set leftPosFunc( value : Function ) : void {
            _leftPosFunc = value;
        }

        public function set rightPosFunc( value : Function ) : void {
            _rightPosFunc = value;
        }

        public function set topPosFunc( value : Function ) : void {
            _topPosFunc = value;
        }

        private function addArrow() : void {
            if ( !_currentArrow ) {
                _currentArrow = new Image();
                _currentArrow.alpha = 0.6;
                addChild( _currentArrow );
            }
        }

        private function arrowLayoutMouseOutHandler( e : MouseEvent ) : void {
            removeArrow();
        }

        private function horizontalMouseMoveHandler( point : Point ) : void {
            var threashold : Number = stage.displayState == StageDisplayState.NORMAL ? ARROW_THREASHOLD : width / 4;

            if ( point.x > _leftPosFunc() && point.x < _leftPosFunc() + threashold && _hasLeftFunc() ) {
                addArrow();

                _currentArrow.source = Resource.ICON_ARROW_LEFT;
                _currentArrow.x = _leftPosFunc() + ARROW_PADDING;
                _currentArrow.y = ( _topPosFunc() + _bottomPosFunc() - ARROW_HORIZONTAL_SIZE.height ) / 2;

                _arrowMode = ARROW_MODE_LEFT;
            } else if ( point.x > _rightPosFunc() - threashold && point.x <= _rightPosFunc() && _hasRightFunc() ) {
                addArrow();

                _currentArrow.source = Resource.ICON_ARROW_RIGHT;
                _currentArrow.x = _rightPosFunc() - ARROW_HORIZONTAL_SIZE.width - ARROW_PADDING;
                _currentArrow.y = ( _topPosFunc() + _bottomPosFunc() - ARROW_HORIZONTAL_SIZE.height ) / 2;

                _arrowMode = ARROW_MODE_RIGHT;
            } else {
                removeArrow();
            }
        }

        private function mouseClickHandler( e : MouseEvent ) : void {
            switch ( _arrowMode ) {
                case ARROW_MODE_LEFT:
                    _clickHandler( -1 );

                    if ( !_hasLeftFunc() ) {
                        removeArrow();
                    }
                    break;
                case ARROW_MODE_RIGHT:
                    _clickHandler( 1 );

                    if ( !_hasRightFunc() ) {
                        removeArrow();
                    }
                    break;
            }
        }

        private function mouseMoveHandler( e : MouseEvent ) : void {
            _arrowMode = ARROW_MODE_UNDEFINED;

            var point : Point = globalToLocal( new Point( e.stageX , e.stageY ) );

            horizontalMouseMoveHandler( point );
        }

        private function removeArrow() : void {
            if ( _currentArrow ) {
                removeChild( _currentArrow );
                _currentArrow = null;
            }
        }
    }
}