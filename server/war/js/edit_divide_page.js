var id = readCookie('document_id');
var devide_page_data;
var devide_page_list;

function addRow() {
	devide_page_list[devide_page_list.length] = {page:0, number:0};
	setDividePage(devide_page_list);
}

function removeRow() {
	var y = 0;
	
	 y = document.getElementById('remove_target').value;
	 y  = ~~y; // type conversion(from string, to number)
	 deleteRow(y);
}

function deleteRow(y) {
	devide_page_list.splice(y, 1);
	setDividePage(devide_page_list);
}

function callback_onedit(editing, val, c, y) {
	if (c == 'Page')
		devide_page_list[y].page = val;
	else if (c == 'Number')
		devide_page_list[y].number = val;

	DocumentService.setDividePage(id, devide_page_list, function() {});
	
	editing[0].innerText = val;
}

$(document).ready(function(){
	DocumentService.getDividePage(id,
			  function(data) {
			    setDividePage(data);
			  }
			);
});

function setDividePage(data) {
	devide_page_list = data;
	devide_page_data = new Array(data.length);
	
    for (var i = 0; i < data.length; i++) {
	   devide_page_data[i] = [data[i].page + "", data[i].number + ""];
    }
    $("#zentable").zentable({
        cols: [{name:'Page', editable:true}, {name:'Number', editable:true}],
        data: devide_page_data,
        onedit: callback_onedit,
        rows: 6
    });
}
