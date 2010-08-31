package jp.archilogic.docnext.ui {
    import flash.display.DisplayObject;
    import mx.events.FlexEvent;
    import flexlib.containers.FlowBox;

    public class FormedFlowBox extends FlowBox {
        public function FormedFlowBox() {
            super();

            addEventListener( FlexEvent.UPDATE_COMPLETE , updateCompleteHandler );
        }

        /**
         * fix bug with FlowBox
         * see http://code.google.com/p/flexlib/issues/detail?id=152
         */
        private function updateCompleteHandler( event : FlexEvent ) : void {
            if ( numChildren > 0 ) {
                // var lastChild : DisplayObject = getChildAt( numChildren - 1 );
                // height = lastChild.y + lastChild.height + viewMetrics.bottom;
                var bottom : Number = 0;

                for each ( var child : DisplayObject in getChildren() ) {
                    bottom = Math.max( bottom , child.y + child.height + viewMetrics.bottom );
                }

                height = bottom;
            } else {
                height = viewMetrics.top + viewMetrics.bottom;
            }
        }
    }
}