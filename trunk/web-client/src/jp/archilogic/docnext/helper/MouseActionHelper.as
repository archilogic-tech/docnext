package jp.archilogic.docnext.helper {
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import mx.containers.Canvas;
    import mx.managers.ISystemManager;
    import jp.archilogic.docnext.type.MouseMode;
    import jp.archilogic.docnext.ui.PageComponent;

    public class MouseActionHelper {
        public function MouseActionHelper( scroller : Canvas , page : PageComponent ) {
            _scroller = scroller;
            _page = page;

            _page.addEventListener( MouseEvent.MOUSE_DOWN , mouseDownHandler );
        }

        private var _scroller : Canvas;
        private var _systemManager : ISystemManager;
        private var _page : PageComponent;
        private var _mouseModeHandler : Function;
        private var _isSelectingHandler : Function;
        private var _currentTargetHandler : Function;

        public function set currentTargetHandler( value : Function ) : * {
            _currentTargetHandler = value;
        }

        public function set isSelectingHandler( value : Function ) : * {
            _isSelectingHandler = value;
        }

        public function set mouseModeHandler( value : Function ) : * {
            _mouseModeHandler = value;
        }

        public function set systemManager( value : ISystemManager ) : * {
            _systemManager = value;
        }

        private function mouseDownHandler( e : MouseEvent ) : void {
            if ( _mouseModeHandler() == MouseMode.SCROLL ) {
                mouseDownHandlerOnScroll( e );
            } else if ( _mouseModeHandler() == MouseMode.SELECT ) {
                mouseDownHandlerOnSelect( e );
            }
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
            var edgeIndex : int = _page.getNearTextPos( new Point( _page.mouseX , _page.mouseY ) );

            if ( edgeIndex != -1 ) {
                _page.initSelection();

                _systemManager.addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                _systemManager.addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );

                _isSelectingHandler( false );

                function mouseMoveHandler( _e : MouseEvent ) : void {
                    var index : int = _page.getNearTextPos( new Point( _page.mouseX , _page.mouseY ) );
                    _page.showSelection( Math.min( edgeIndex , index ) , Math.max( edgeIndex , index ) );

                    _isSelectingHandler( true );
                    _currentTargetHandler( _page );
                }

                function mouseUpHandler( _e : MouseEvent ) : void {
                    _systemManager.removeEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
                    _systemManager.removeEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
                }
            }
        }
    }
}