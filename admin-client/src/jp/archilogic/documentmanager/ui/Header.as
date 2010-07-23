package jp.archilogic.documentmanager.ui {
    import com.asual.swfaddress.SWFAddress;
    import flash.events.MouseEvent;
    import flash.net.URLRequest;
    import flash.net.navigateToURL;
    import mx.containers.Box;
    import mx.events.FlexEvent;
    import jp.archilogic.ServiceUtil;

    public class Header extends Box {
        public function Header() {
            super();

            _ui = new HeaderUI();
            _ui.addEventListener( FlexEvent.CREATION_COMPLETE , creationCompleteHandler );
            addChild( _ui );
        }

        private var _ui : HeaderUI;

        private function creationCompleteHandler( event : FlexEvent ) : void {
            SWFAddress.setStrict( false );

            _ui.logoImage.addEventListener( MouseEvent.CLICK , logoImageClickHandler );
        }

        private function logoImageClickHandler( event : MouseEvent ) : void {
            navigateToURL( new URLRequest( './' ) , '_self' );
        }
    }
}
