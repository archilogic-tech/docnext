$(document).ready(function() {
	$("#edit_header").load("edit/menu.html", function() {
		$("a.navi").click(function(event){
			var type = this.href.match(/([^\/.]*).html/)[1];
			loadPage(type);
			event.preventDefault();
		});
	});
	loadPage("info");
});

function loadPage(type) {
	$("#content").load("edit/" + type + ".html");
	$.getScript("js/edit/" + type + ".js");
}