package jp.archilogic.documentmanager.controller {
    import flash.events.MouseEvent;
    import flash.net.URLRequest;
    import flash.net.navigateToURL;
    import mx.collections.ArrayCollection;
    import mx.controls.Button;
    import mx.utils.ObjectUtil;
    import jp.archilogic.Delegate;
    import jp.archilogic.documentmanager.dto.DocumentResDto;
    import jp.archilogic.documentmanager.dto.TOCElem;
    import jp.archilogic.documentmanager.service.DocumentService;
    import jp.archilogic.documentmanager.util.RadixUtil;

    public class IndexController extends Delegate {
        public var view : Index;

        override protected function creationComplete() : void {
            view.uploadButton.addEventListener( MouseEvent.CLICK , uploadButtonClickHandler );

            DocumentService.findAll( function( result : ArrayCollection ) : void {
                    for each ( var dto : DocumentResDto in result ) {
                        ( function( _dto : DocumentResDto ) : void {
                                var button : Button = new Button();
                                button.label = _dto.name;
                                button.addEventListener( MouseEvent.CLICK , function( event : MouseEvent ) : void {
                                        navigateToURL( new URLRequest( 'Editor.swf?id=' + _dto.id ) , '_self' );
                                    } );
                                view.holder.addChild( button );
                            } )( dto );
                    }
                } );
        }

        private function uploadButtonClickHandler( event : MouseEvent ) : void {
            navigateToURL( new URLRequest( 'upload.html' ) , '_self' );
        }
    }
}
