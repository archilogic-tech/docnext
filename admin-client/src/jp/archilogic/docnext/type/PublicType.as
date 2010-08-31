package jp.archilogic.docnext.type {

    public class PublicType {
        public static const PUBLIC : uint = 0;
        public static const PASSWORD : uint = 1;
        public static const PRIVATE : uint = 2;

        public static function values() : Array {
            return [ '公開' , 'パスワード' , '非公開' ];
        }
    }
}