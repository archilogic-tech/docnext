var id = readCookie('document_id');
var spi_list;

function setSPI(spi) {
	var spi_data;

	spi_list = spi;
	spi_data = new Array(spi.length);
	
    for (var i = 0; i < spi.length; i++) {
	   spi_data[i] = [spi[i]];
    }
    $("#zentable").zentable({
        cols: [{name:'Page', editable:true}],
        data: spi_data,
        onedit: callbackOnEdit,
        rows: 6
    });
}

function addRow() {
	spi_list[spi_list.length] = 0;
	setSPI(spi_list);
}

function removeRow() {
	var y = 0;
	
	y = document.getElementById('remove_target').value;
	y = ~~y;
	deleteRow(y);
}

function deleteRow(y) {
	spi_list.splice(y, 1);
	setSPI(spi_list);
}

function callbackOnEdit(editing, val, c, y) {
	spi_list[y] = ~~val;
	
	DocumentService.setSinglePageInfo(id, spi_list, 
			function () {});
	
	editing[0].innerText = val;
}

$(document).ready(function(){
	DocumentService.getSinglePageInfo(id,
			  function(data) {
			    setSPI(data);
			  }
			);
});
