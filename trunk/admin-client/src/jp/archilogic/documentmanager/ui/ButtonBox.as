package jp.archilogic.documentmanager.ui {
    import flash.events.MouseEvent;
    import mx.containers.Box;
    import mx.core.ScrollPolicy;

    [Style( name="rollOverColor" , type="uint" , format="Color" , inherit="yes" )]
    [Style( name="selectionColor" , type="uint" , format="Color" , inherit="yes" )]
    public class ButtonBox extends Box {
        public function ButtonBox() {
            super();

            horizontalScrollPolicy = verticalScrollPolicy = ScrollPolicy.OFF;

            setStyle( 'cornerRadius' , 4 );
            setStyle( 'rollOverColor' , 0xb2e1ff );
            setStyle( 'selectionColor' , 0x7fceff );

            addEventListener( MouseEvent.MOUSE_OVER , mouseOverHandler );
            addEventListener( MouseEvent.MOUSE_OUT , mouseOutHandler );
            addEventListener( MouseEvent.MOUSE_DOWN , mouseDownHandler );
            addEventListener( MouseEvent.MOUSE_UP , mouseUpHandler );
        }

        private function drawBackground( color : uint ) : void {
            graphics.beginFill( color );
            graphics.drawRoundRect( 0 , 0 , width , height , getStyle( 'cornerRadius' ) * 2 );
            graphics.endFill();
        }

        private function mouseDownHandler( event : MouseEvent ) : void {
            if ( enabled ) {
                drawBackground( getStyle( 'selectionColor' ) );
            }
        }

        private function mouseOutHandler( event : MouseEvent ) : void {
            graphics.clear();
        }

        private function mouseOverHandler( event : MouseEvent ) : void {
            if ( enabled ) {
                drawBackground( getStyle( 'rollOverColor' ) );
            }
        }

        private function mouseUpHandler( event : MouseEvent ) : void {
            if ( enabled ) {
                drawBackground( getStyle( 'rollOverColor' ) );
            }
        }
    }
}