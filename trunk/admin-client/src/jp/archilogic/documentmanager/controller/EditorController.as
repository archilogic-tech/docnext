package jp.archilogic.documentmanager.controller {
    import flash.events.MouseEvent;
    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.events.FlexEvent;
    import jp.archilogic.Delegate;
    import jp.archilogic.documentmanager.dto.TOCElem;
    import jp.archilogic.documentmanager.service.DocumentService;

    public class EditorController extends Delegate {
        public var view : Editor;

        private var _id : Number;

        override protected function creationComplete() : void {
            view.loadInfoButton.addEventListener( MouseEvent.CLICK , loadInfoButtonClickHandler );
            view.saveInfoButton.addEventListener( MouseEvent.CLICK , saveInfoButtonClickHandler );

            view.textBox.addEventListener( FlexEvent.CREATION_COMPLETE , textBoxCreationCompleteHandler );
            view.tocBox.addEventListener( FlexEvent.CREATION_COMPLETE , tocBoxCreationCompeteHandler );
            view.singlePageInfoBox.addEventListener( FlexEvent.CREATION_COMPLETE ,
                                                     singlePageInfoBoxCreationCompeteHandler );
            view.finalizeButton.addEventListener( MouseEvent.CLICK , finalizeButtonClickHandler );

            _id = view.parameters[ 'id' ];
        }

        private function addSinglePageInfoButtonClickHandler( event : MouseEvent ) : void {
            ArrayCollection( view.singlePageInfoDataGrid.dataProvider ).addItem( { value: 0 } );
        }

        private function addTOCButtonClickHandler( event : MouseEvent ) : void {
            ArrayCollection( view.tocDataGrid.dataProvider ).addItem( new TOCElem() );
        }

        private function finalizeButtonClickHandler( event : MouseEvent ) : void {
            DocumentService.repack( _id , function() : void {
                    Alert.show( 'Complete' );
                } );
        }

        private function loadInfoButtonClickHandler( event : MouseEvent ) : void {
            DocumentService.getTitle( _id , function( title : String ) : void {
                    view.titleTextInput.text = title;

                    DocumentService.getPublisher( _id , function( publisher : String ) : void {
                            view.publisherTextInput.text = publisher;
                        } );
                } );
        }

        private function loadSinglePageInfoButtonClickHandler( event : MouseEvent ) : void {
            DocumentService.getSinglePageInfo( _id , function( result : ArrayCollection ) : void {
                    var dp : Array = [];

                    for each ( var value : int in result ) {
                        dp.push( { value: value } );
                    }

                    view.singlePageInfoDataGrid.dataProvider = dp;
                } );
        }

        private function loadTOCButtonClickHandler( event : MouseEvent ) : void {
            DocumentService.getTOC( _id , function( result : ArrayCollection ) : void {
                    view.tocDataGrid.dataProvider = result;
                } );
        }

        private function loadTextButtonClickHandler( event : MouseEvent ) : void {
            var page : int = parseInt( view.pageTextInput.text );
            DocumentService.getText( _id , page , function( result : String ) : void {
                    view.pageTextArea.text = result;
                } );
        }

        private function removeSinglePageInfoButtonClickHandler( event : MouseEvent ) : void {
            if ( view.singlePageInfoDataGrid.selectedItem ) {
                ArrayCollection( view.singlePageInfoDataGrid.dataProvider ).removeItemAt( view.singlePageInfoDataGrid.selectedIndex );
            }
        }

        private function removeTOCButtonClickHandler( event : MouseEvent ) : void {
            if ( view.tocDataGrid.selectedItem ) {
                ArrayCollection( view.tocDataGrid.dataProvider ).removeItemAt( view.tocDataGrid.selectedIndex );
            }
        }

        private function saveInfoButtonClickHandler( event : MouseEvent ) : void {
            if ( view.titleTextInput.text.length == 0 || view.publisherTextInput.text.length == 0 ) {
                Alert.show( 'Please fill fields' );
                return;
            }

            DocumentService.setTitle( _id , view.titleTextInput.text , function() : void {
                    DocumentService.setPublisher( _id , view.publisherTextInput.text , function() : void {
                            Alert.show( 'Done' );
                        } );
                } );
        }

        private function saveSinglePageInfoButtonClickHandler( event : MouseEvent ) : void {
            var data : Array = [];

            for each ( var value : Object in view.singlePageInfoDataGrid.dataProvider ) {
                data.push( value.value );
            }

            DocumentService.setSinglePageInfo( _id , new ArrayCollection( data ) , function() : void {
                    Alert.show( 'Done' );
                } );
        }

        private function saveTOCButtonClickHandler( event : MouseEvent ) : void {
            DocumentService.setTOC( _id , ArrayCollection( view.tocDataGrid.dataProvider ) , function() : void {
                    Alert.show( 'Done' );
                } );
        }

        private function saveTextButtonClickHandler( event : MouseEvent ) : void {
            var page : int = parseInt( view.pageTextInput.text );
            DocumentService.setText( _id , page , view.pageTextArea.text , function( result : String ) : void {
                    Alert.show( 'Done' );
                } );
        }

        private function singlePageInfoBoxCreationCompeteHandler( event : FlexEvent ) : void {
            view.loadSinglePageInfoButton.addEventListener( MouseEvent.CLICK , loadSinglePageInfoButtonClickHandler );
            view.saveSinglePageInfoButton.addEventListener( MouseEvent.CLICK , saveSinglePageInfoButtonClickHandler );
            view.addSinglePageInfoButton.addEventListener( MouseEvent.CLICK , addSinglePageInfoButtonClickHandler );
            view.removeSinglePageInfoButton.addEventListener( MouseEvent.CLICK ,
                                                              removeSinglePageInfoButtonClickHandler );
        }

        private function textBoxCreationCompleteHandler( event : FlexEvent ) : void {
            view.loadTextButton.addEventListener( MouseEvent.CLICK , loadTextButtonClickHandler );
            view.saveTextButton.addEventListener( MouseEvent.CLICK , saveTextButtonClickHandler );
        }

        private function tocBoxCreationCompeteHandler( event : FlexEvent ) : void {
            view.loadTOCButton.addEventListener( MouseEvent.CLICK , loadTOCButtonClickHandler );
            view.saveTOCButton.addEventListener( MouseEvent.CLICK , saveTOCButtonClickHandler );
            view.addTOCButton.addEventListener( MouseEvent.CLICK , addTOCButtonClickHandler );
            view.removeTOCButton.addEventListener( MouseEvent.CLICK , removeTOCButtonClickHandler );
        }
    }
}
