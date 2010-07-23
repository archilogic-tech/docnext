package jp.archilogic.documentmanager.controller {
    import flash.display.Loader;
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.net.FileFilter;
    import flash.net.FileReference;
    import flash.net.FileReferenceList;
    import flash.net.URLRequest;
    import flash.net.navigateToURL;
    import flash.utils.ByteArray;
    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.events.FlexEvent;
    import mx.events.ListEvent;
    import jp.archilogic.Delegate;
    import jp.archilogic.documentmanager.service.DocumentService;
    import jp.archilogic.documentmanager.util.RadixUtil;

    public class UploadController extends Delegate {
        public var view : Upload;

        private var _fileData : ByteArray;

        override protected function creationComplete() : void {
            view.browseButton.addEventListener( MouseEvent.CLICK , browseButtonClickHandler );
            view.uploadButton.addEventListener( MouseEvent.CLICK , uploadButtonClickHandler );
        }

        private function browseButtonClickHandler( event : MouseEvent ) : void {
            var file : FileReference = new FileReference();

            file.addEventListener( Event.SELECT , function( evnet : Event ) : void {
                    file.addEventListener( Event.COMPLETE , function( event : Event ) : void {
                            file.removeEventListener( Event.COMPLETE , arguments.callee );

                            _fileData = new ByteArray();
                            file.data.readBytes( _fileData , 0 , file.data.length );
                            view.fileNameLabel.text = file.name;
                        } );

                    file.load();
                } );

            file.browse( [ new FileFilter( "Document file" ,
                                           "*.pdf;*.xls;*.xlsx;*.doc;*.docx;*.ppt;*.pptx;*.txt;*.rtf;*.odt;*.ods;*.odp" ) ] );
        }

        private function uploadButtonClickHandler( event : MouseEvent ) : void {
            if ( view.nameTextInput.text.length == 0 ) {
                Alert.show( 'タイトルが入力されていません' );
                return;
            }

            if ( !_fileData ) {
                Alert.show( 'ファイルが選択されていません' );
                return;
            }

            DocumentService.upload( _fileData , view.fileNameLabel.text , view.nameTextInput.text ,
                                           function( result : Number ) : void {
                    Alert.show( 'Complete!!!' );
                } );
        }
    }
}
