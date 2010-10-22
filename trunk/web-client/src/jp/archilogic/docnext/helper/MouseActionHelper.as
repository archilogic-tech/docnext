package jp.archilogic.docnext.helper {
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import mx.containers.Canvas;
    import mx.managers.ISystemManager;
    import mx.managers.SystemManager;
    import jp.archilogic.docnext.type.MouseMode;
    import jp.archilogic.docnext.ui.PageComponent;

    public class MouseActionHelper {
        public function MouseActionHelper( scroller : Canvas , systemManager : ISystemManager ,
                                           currentPageHandler : Function ) {
            _scroller = scroller;
            _systemManager = systemManager;
            _currentPageHandler = currentPageHandler;
        }

        private var _scroller : Canvas;
        private var _systemManager : ISystemManager;
        private var _currentPageHandler : Function;
        private var _mouseModeHandler : Function;
        private var _isSelectingHandler : Function;

        public function set isSelectingHandler( value : Function ) : * {
            _isSelectingHandler = value;
        }

        public function mouseDownHandler( e : MouseEvent ) : void {
            if ( _mouseModeHandler() == MouseMode.SCROLL ) {
                mouseDownHandlerOnScroll( e );
            } else if ( _mouseModeHandler() == MouseMode.SELECT ) {
                mouseDownHandlerOnSelect( e );
            }
        }

        public function set mouseModeHandler( value : Function ) : * {
            _mouseModeHandler = value;
        }

        private function mouseDownHandlerOnScroll( e : MouseEvent ) : void {
            var mousePoint : Point = new Point( e.stageX , e.stageY );
            var scrollPoint : Point =
                new Point( _scroller.horizontalScrollPosition , _scroller.verticalScrollPosition );

            _systemManager.addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
            _systemManager.addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );

            function mouseMoveHandler( _e : MouseEvent ) : void {
                _scroller.horizontalScrollPosition = scrollPoint.x - _e.stageX + mousePoint.x;
                _scroller.verticalScrollPosition = scrollPoint.y - _e.stageY + mousePoint.y;
            }

            function mouseUpHandler( _e : MouseEvent ) : void {
                _systemManager.removeEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                _systemManager.removeEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
            }
        }

        private function mouseDownHandlerOnSelect( e : MouseEvent ) : void {
            var current : PageComponent = _currentPageHandler();

            var edgeIndex : int = current.getNearTextPos( current.globalToLocal( new Point( e.stageX , e.stageY ) ) );

            if ( edgeIndex != -1 ) {
                current.initSelection();

                _systemManager.addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                _systemManager.addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );

                _isSelectingHandler( false );

                function mouseMoveHandler( _e : MouseEvent ) : void {
                    var index : int =
                        current.getNearTextPos( current.globalToLocal( new Point( _e.stageX , _e.stageY ) ) );
                    current.showSelection( Math.min( edgeIndex , index ) , Math.max( edgeIndex , index ) );

                    _isSelectingHandler( true );
                }

                function mouseUpHandler( _e : MouseEvent ) : void {
                    _systemManager.removeEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                    _systemManager.removeEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
                }
            }
        }
    }
}