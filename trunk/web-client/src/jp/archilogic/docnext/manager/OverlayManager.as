package jp.archilogic.docnext.manager {
    import flash.geom.Point;
    import flash.geom.Rectangle;

    public class OverlayManager {
        public static function convertToStageRect( rect : Rectangle , size : Point , ratio : Number ) : Rectangle {
            var actual : Rectangle = calcActualRect( size , ratio );

            return new Rectangle( actual.x + rect.x * actual.width , actual.y + rect.y * actual.height ,
                                  rect.width * actual.width , rect.height * actual.height );
        }

        private static function calcActualRect( size : Point , ratio : Number ) : Rectangle {
            var ret : Rectangle = new Rectangle( 0 , 0 , size.x , size.y );

            if ( ret.width < ret.height * ratio ) {
                // fit to width
                ret.y = ( ret.height - ret.width / ratio ) / 2.0;
                ret.height = ret.width / ratio;
            } else {
                // fit to height
                ret.x = ( ret.width - ret.height * ratio ) / 2.0;
                ret.width = ret.height * ratio;
            }

            return ret;
        }
    }
}