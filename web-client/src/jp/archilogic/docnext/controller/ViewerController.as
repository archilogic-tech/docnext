package jp.archilogic.docnext.controller {
    import com.asual.swfaddress.SWFAddress;
    import flash.events.MouseEvent;
    import mx.controls.Alert;
    import mx.rpc.Fault;
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
            view.documentComponent.mouseModeHandler = view.toolbox.mouseModeHander;
            view.toolbox.copyHandler = copyHandler;

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

        private function copyHandler() : void {
            view.documentComponent.copy();
        }

        private function zoomInHandler() : void {
            view.documentComponent.zoomIn();
        }

        private function zoomOutHandler() : void {
            view.documentComponent.zoomOut();
        }
    }
}