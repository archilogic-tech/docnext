package jp.archilogic.docnext.service {
    import flash.utils.ByteArray;
    import mx.collections.ArrayCollection;
    import jp.archilogic.Service;
    import jp.archilogic.ServiceUtil;

    public class DocumentService {
        private static var service : Service = new Service( 'documentService' );

        public static function findAll( result : Function ) : void {
            ServiceUtil.callHelper( service.findAll() , result );
        }

        public static function findById( id : Number , result : Function , fault : Function ) : void {
            ServiceUtil.callHelper( service.findById( id ) , result , fault );
        }

        public static function getPublisher( id : Number , result : Function ) : void {
            ServiceUtil.callHelper( service.getPublisher( id ) , result );
        }

        public static function getSinglePageInfo( id : Number , result : Function ) : void {
            ServiceUtil.callHelper( service.getSinglePageInfo( id ) , result );
        }

        public static function getTOC( id : Number , result : Function ) : void {
            ServiceUtil.callHelper( service.getTOC( id ) , result );
        }

        public static function getText( id : Number , page : int , result : Function ) : void {
            ServiceUtil.callHelper( service.getText( id , page ) , result );
        }

        public static function getTitle( id : Number , result : Function ) : void {
            ServiceUtil.callHelper( service.getTitle( id ) , result );
        }

        public static function repack( id : Number , result : Function ) : void {
            ServiceUtil.callHelper( service.repack( id ) , result );
        }

        public static function setPublisher( id : Number , publisher : String , result : Function ) : void {
            ServiceUtil.callHelper( service.setPublisher( id , publisher ) , result );
        }

        public static function setSinglePageInfo( id : Number , singlePageInfo : ArrayCollection ,
                                                  result : Function ) : void {
            ServiceUtil.callHelper( service.setSinglePageInfo( id , singlePageInfo ) , result );
        }

        public static function setTOC( id : Number , toc : ArrayCollection , result : Function ) : void {
            ServiceUtil.callHelper( service.setTOC( id , toc ) , result );
        }

        public static function setText( id : Number , page : int , text : String , result : Function ) : void {
            ServiceUtil.callHelper( service.setText( id , page , text ) , result );
        }

        public static function setTitle( id : Number , title : String , result : Function ) : void {
            ServiceUtil.callHelper( service.setTitle( id , title ) , result );
        }
    }
}
