var id = readCookie('document_id');
var toc_data;
var toc_list;

function setTOC(tocs) {
	toc_list = tocs;
	toc_data = new Array(tocs.length);
	
    for (var i = 0; i < tocs.length; i++) {
	   toc_data[i] = [tocs[i].page + "", tocs[i].text];
    }
    $("#zentable").zentable({
        cols: [{name:'Page', editable:true}, {name:'Text', editable:true}],
        data: toc_data,
        onedit: callback_onedit,
        rows: 6
    });
}

function addRow() {
	toc_list[toc_list.length] = {page:0, text:''};
	setTOC(toc_list);
}

function removeRow() {
	var y = 0;
	
	 y = document.getElementById('remove_target').value;
	 y  = ~~y; // type conversion(from string, to number)
	 deleteRow(y);
}

function deleteRow(y) {
	toc_list.splice(y, 1);
	setTOC(toc_list);
}

function callback_onedit(editing, val, c, y) {
	if (c == 'Text')
		toc_list[y].text = val;
	else if (c == 'Page')
		toc_list[y].page = val;

	DocumentService.setTOC(id, toc_list,
			  function() {});
	
	editing[0].innerText = val;
}

$(document).ready(function(){
	DocumentService.getTOC(id,
			  function(data) {
			    setTOC(data);
			  }
			);
});
