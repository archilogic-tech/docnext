package jp.archilogic.docnext.controller {
    import com.asual.swfaddress.SWFAddress;
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
            view.documentComponent.isSelectingHandler = view.toolbox.isSelectingHandler;
            view.toolbox.copyHandler = copyHandler;
            view.toolbox.changeToHighlightHandler = changeToHighlightHandler;
            view.documentComponent.isSelectHighlightHandler = view.toolbox.isSelectHighlightHandler;
            view.toolbox.chnageHighlightColorHandler = changeHighlightColorHandler;
            view.toolbox.removeHighlightHandler = removeHighlightHandler;
            view.toolbox.changeHighlightCommentHandler = changeHighlightCommentHandler;
            view.documentComponent.initHighlightCommentHandler = initHighlightCommentHandler;

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

        private function changeHighlightColorHandler( color : uint ) : void {
            view.documentComponent.changeHighlightColor( color );
        }

        private function changeHighlightCommentHandler( comment : String ) : void {
            view.documentComponent.changeHighlightComment( comment );
        }

        private function changeToHighlightHandler() : void {
            view.documentComponent.changeToHighlight();
        }

        private function copyHandler() : void {
            view.documentComponent.copy();
        }

        private function initHighlightCommentHandler( comment : String ) : void {
            view.toolbox.initHighlightComment( comment );
        }

        private function removeHighlightHandler() : void {
            view.documentComponent.removeHighlight();
        }

        private function zoomInHandler() : void {
            view.documentComponent.zoomIn();
        }

        private function zoomOutHandler() : void {
            view.documentComponent.zoomOut();
        }
    }
}