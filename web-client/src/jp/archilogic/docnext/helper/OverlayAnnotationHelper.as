package jp.archilogic.docnext.helper {
    import flash.display.DisplayObjectContainer;
    import flash.events.MouseEvent;
    import flash.geom.Rectangle;
    import flash.net.URLRequest;
    import flash.net.navigateToURL;
    import mx.containers.Canvas;

    public class OverlayAnnotationHelper {
        private static const ALPHA : Number = 0.2;

        public function OverlayAnnotationHelper( container : DisplayObjectContainer , converter : Function ) {
            _container = container;
            _converter = converter;
        }

        private var _container : DisplayObjectContainer;
        private var _converter : Function;
        private var _changePageHanlder : Function;

        public function set annotation( value : Array ) : * {
            for each ( var anno : Object in value ) {
                ( function( anno : Object ) : void {
                    var rect : Rectangle =
                        _converter( new Rectangle( anno.region.x , anno.region.y , anno.region.width ,
                                                   anno.region.height ) );

                    switch ( anno.action.action ) {
                        case 'URI':
                            addAnnotation( rect , function( e : MouseEvent ) : void {
                                navigateToURL( new URLRequest( anno.action.uri ) );
                            } );
                            break;
                        case 'GoToPage':
                            addAnnotation( rect , function( e : MouseEvent ) : void {
                                // change to 0-origin
                                _changePageHanlder( anno.action.page - 1 );
                            } );
                            break;
                        default:
                            throw new Error();
                    }
                } )( anno );
            }
        }

        public function set changePageHelper( value : Function ) : * {
            _changePageHanlder = value;
        }

        private function addAnnotation( rect : Rectangle , clickHandler : Function ) : void {
            var indicator : Canvas = new Canvas();
            indicator.x = rect.x;
            indicator.y = rect.y;
            indicator.width = rect.width;
            indicator.height = rect.height;
            indicator.setStyle( 'backgroundColor' , 0x000000 );
            indicator.alpha = ALPHA;

            _container.addChild( indicator );

            indicator.addEventListener( MouseEvent.CLICK , clickHandler );
        }
    }
}