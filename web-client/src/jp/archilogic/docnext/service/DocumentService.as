package jp.archilogic.docnext.service {
    import jp.archilogic.Service;
    import jp.archilogic.ServiceUtil;

    public class DocumentService {
        private static var service : Service = new Service( 'documentService' );

        public static function findById( id : Number , result : Function , fault : Function ) : void {
            ServiceUtil.callHelper( service.findById( id ) , result , fault );
        }

        public static function getImageText( id : Number , page : int , result : Function ) : void {
            ServiceUtil.callHelper( service.getImageText( id , page ) , result );
        }

        public static function getInfo( id : Number , result : Function ) : void {
            ServiceUtil.callHelper( service.getInfo( id ) , result );
        }

        public static function getPage( id : Number , page : int , result : Function ) : void {
            ServiceUtil.callHelper( service.getPage( id , page ) , result );
        }

        public static function getRegions( id : Number , page : int , result : Function ) : void {
            ServiceUtil.callHelper( service.getRegions( id , page ) , result );
        }
    }
}
