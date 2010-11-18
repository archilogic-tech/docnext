package jp.archilogic.docnext.helper {
    import flash.events.Event;
    import flash.events.IEventDispatcher;
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import mx.core.Container;
    import mx.core.UIComponent;
    import caurina.transitions.Tweener;
    import jp.archilogic.docnext.ui.DocumentComponentArrowIndicator;
    import jp.archilogic.docnext.ui.PageComponent;
    import jp.archilogic.docnext.ui.SelectionContextMenu;

    public class DocumentMouseEventHelper {
        private static const CLICK_THRESHOLD : Number = 5;

        public function DocumentMouseEventHelper( target : IEventDispatcher ) {
            _selecting = false;

            target.addEventListener( MouseEvent.CLICK , clickHandler );
            target.addEventListener( MouseEvent.MOUSE_DOWN , mouseDownHandler );
            target.addEventListener( MouseEvent.MOUSE_MOVE , mouseMoveHandler );
            target.addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
        }

        private var _scroller : Container;

        // menu var
        private var _isMenuVisibleFunc : Function;
        private var _changeMenuVisiblityFunc : Function;
        // change page var
        private var _arrowIndicator : DocumentComponentArrowIndicator;
        private var _changePageFunc : Function;
        // selecting var
        private var _selecting : Boolean;
        private var _selectingTarget : PageComponent;
        private var _selectingEdge : int;
        private var _hasSelection : Boolean;
        private var _selectionMenu : SelectionContextMenu;

        private var _isClick : Boolean;
        private var _mouseDownPoint : Point;
        private var _mouseDownScrollPos : Point;

        public function set arrowIndicator( value : DocumentComponentArrowIndicator ) : * {
            _arrowIndicator = value;
        }

        public function set changeMenuVisiblityFunc( value : Function ) : * {
            _changeMenuVisiblityFunc = value;
        }

        public function set changePageFunc( value : Function ) : * {
            _changePageFunc = value;
        }

        public function set isMenuVisibleFunc( value : Function ) : * {
            _isMenuVisibleFunc = value;
        }

        public function set scroller( value : Container ) : * {
            _scroller = value;
        }

        public function set selecting( value : Boolean ) : * {
            _selecting = value;
        }

        private function clickHandler( e : MouseEvent ) : void {
            if ( !_isClick ) {
                return;
            }
            _isClick = false;

            if ( _isMenuVisibleFunc() ) {
                _changeMenuVisiblityFunc( false );
            } else if ( _selecting ) {
                _selecting = false;
                _changeMenuVisiblityFunc( true );
            } else if ( _arrowIndicator.isShowingIndicator ) {
                _changePageFunc( _arrowIndicator.isShowingLeftIndicator ? 1 : -1 );
            } else {
                _changeMenuVisiblityFunc( true );
            }
        }

        private function dismissSelectionContextMenuFunc() : void {
            _selecting = false;
            _selectingTarget.initSelection();
            removeSelectionMenu();
        }

        private function hypot( x : Point , y : Point ) : Number {
            return Math.sqrt( Math.pow( x.x - y.x , 2 ) + Math.pow( x.y - y.y , 2 ) );
        }

        private function mouseDownHandler( e : MouseEvent ) : void {
            _isClick = true;
            _mouseDownPoint = new Point( e.stageX , e.stageY );

            if ( !_isMenuVisibleFunc() ) {
                if ( _selecting ) {
                    if ( _selectionMenu ) {
                        removeSelectionMenu();
                    }

                    _selectingTarget = travPage( e );
                    _hasSelection = false;

                    if ( _selectingTarget ) {
                        _selectingEdge =
                            _selectingTarget.getNearTextPos( new Point( _selectingTarget.mouseX ,
                                                                        _selectingTarget.mouseY ) );

                        if ( _selectingEdge != -1 ) {
                            _selectingTarget.initSelection();
                        }
                    }
                } else {
                    _mouseDownScrollPos =
                        new Point( _scroller.horizontalScrollPosition , _scroller.verticalScrollPosition );
                }
            }
        }

        private function mouseMoveHandler( e : MouseEvent ) : void {
            if ( _isClick ) {
                _isClick = hypot( _mouseDownPoint , new Point( e.stageX , e.stageY ) ) < CLICK_THRESHOLD;
            }

            if ( !_isMenuVisibleFunc() ) {
                if ( !e.buttonDown ) {
                    if ( !_selecting ) {
                        _arrowIndicator.mouseMove();
                    }
                } else {
                    if ( _selecting ) {
                        if ( _selectingTarget ) {
                            var index : int =
                                _selectingTarget.getNearTextPos( new Point( _selectingTarget.mouseX ,
                                                                            _selectingTarget.mouseY ) );
                            _selectingTarget.showSelection( Math.min( _selectingEdge , index ) ,
                                                                      Math.max( _selectingEdge , index ) );

                            _hasSelection = true;
                        }
                    } else {
                        _scroller.horizontalScrollPosition = _mouseDownScrollPos.x - e.stageX + _mouseDownPoint.x;
                        _scroller.verticalScrollPosition = _mouseDownScrollPos.y - e.stageY + _mouseDownPoint.y;
                    }
                }
            }
        }

        private function mouseUpHandler( e : MouseEvent ) : void {
            if ( _selecting && _hasSelection ) {
                if ( _selectionMenu ) {
                    Tweener.removeTweens( _selectionMenu );
                    _scroller.removeChild( _selectionMenu );
                }

                _selectionMenu = new SelectionContextMenu( _selectingTarget , dismissSelectionContextMenuFunc );

                _selectionMenu.alpha = 0;
                _scroller.addChild( _selectionMenu );

                Tweener.addTween( _selectionMenu , { alpha: 1 , time: 0.5 } );
            }
        }

        private function removeSelectionMenu() : void {
            Tweener.addTween( _selectionMenu , { alpha: 0 , time: 0.5 , onComplete: function() : void {
                        _scroller.removeChild( _selectionMenu );
                        _selectionMenu = null;
                    } } );
        }

        private function travPage( e : Event ) : PageComponent {
            if ( e.target is UIComponent ) {
                var target : UIComponent = UIComponent( e.target );

                while ( target.parent ) {
                    if ( target is PageComponent ) {
                        return PageComponent( target );
                    }

                    if ( target.parent is UIComponent ) {
                        target = UIComponent( target.parent );
                    } else {
                        return null;
                    }
                }
            }

            return null;
        }
    }
}