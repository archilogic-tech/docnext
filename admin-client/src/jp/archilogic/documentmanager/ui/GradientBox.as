package jp.archilogic.documentmanager.ui {
    import flash.display.GradientType;
    import flash.geom.Matrix;
    import mx.containers.Box;

    public class GradientBox extends Box {
        private var _gradientColors : Array;
        private var _alphas : Array;
        private var _ratios : Array;

        public function set gradientColors( value : Array ) : * {
            _gradientColors = value;

            _alphas = [];

            for ( var i : int = 0 ; i < value.length ; i++ ) {
                _alphas.push( 1 );
            }
        }

        public function set ratios( value : Array ) : * {
            _ratios = [];

            for ( var i : int = 0 ; i < value.length ; i++ ) {
                _ratios.push( value[ i ] * 0xff );
            }
        }

        override protected function updateDisplayList( unscaledWidth : Number , unscaledHeight : Number ) : void {
            super.updateDisplayList( unscaledWidth , unscaledHeight );

            if ( _gradientColors ) {
                graphics.clear();

                var matrix : Matrix = new Matrix();
                matrix.createGradientBox( unscaledWidth , unscaledHeight , Math.PI / 2 , 0 , 0 );

                graphics.beginGradientFill( GradientType.LINEAR , _gradientColors , _alphas , _ratios , matrix );
                graphics.drawRect( 0 , 0 , unscaledWidth , unscaledHeight );
                graphics.endFill();
            }
        }
    }
}