package jp.archilogic.docnext.controller {
    import com.asual.swfaddress.SWFAddress;
    import mx.controls.Alert;
    import mx.rpc.Fault;
    import caurina.transitions.Tweener;
    import jp.archilogic.Delegate;
    import jp.archilogic.ServiceUtil;
    import jp.archilogic.docnext.dto.DocumentResDto;
    import jp.archilogic.docnext.service.DocumentService;
    import jp.archilogic.docnext.util.ConstUtil;

    public class ViewerController extends Delegate {
        public var view : Viewer;

        override protected function creationComplete() : void {
            view.toolbox.zoomInHandler = zoomInHandler;
            view.toolbox.zoomOutHandler = zoomOutHandler;
            view.documentComponent.setPageHandler = setPageHandler;
            view.documentComponent.isMenuVisibleHandler = isMenuVisibleHandler;
            view.documentComponent.changeMenuVisiblityHandler = changeMenuVisiblityHandler;
            view.toolbox.changeMenuVisiblityHandler = changeMenuVisiblityHandler;
            view.toolbox.selectingHandler = selectingHandler;

            var id : Number = view.parameters[ 'id' ];

            DocumentService.findById( id , function( dto : DocumentResDto ) : void {
                SWFAddress.setTitle( ConstUtil.TITLE_VIEWER_PREFIX + encodeURIComponent( dto.name ) );

                view.documentComponent.load( dto );
            } , function( fault : Fault ) : void {
                if ( fault.faultCode == 'NotFound' ) {
                    Alert.show( '対象のドキュメントは存在しません' );
                } else if ( fault.faultCode == 'PrivateDocument' ) {
                    Alert.show( '対象のドキュメントは非公開です' );
                } else {
                    ServiceUtil.defaultFaultHandler( fault );
                }
            } );
        }

        private function changeMenuVisiblityHandler( value : Boolean ) : void {
            if ( value ) {
                view.toolbox.alpha = 0;
                view.toolbox.visible = true;
            }

            Tweener.addTween( view.toolbox , { alpha: value ? 1 : 0 , time: 0.5 , onComplete: function() : void {
                        if ( !value ) {
                            view.toolbox.visible = false;
                            view.toolbox.alpha = 0;
                        }
                    } } );
        }

        private function isMenuVisibleHandler() : Boolean {
            return view.toolbox.visible;
        }

        private function selectingHandler( value : Boolean ) : void {
            view.documentComponent.selecting = value;
        }

        private function setPageHandler( current : int , total : int ) : void {
            view.toolbox.setPage( current , total );
        }

        private function zoomInHandler() : void {
            view.documentComponent.zoomIn();
        }

        private function zoomOutHandler() : void {
            view.documentComponent.zoomOut();
        }
    }
}