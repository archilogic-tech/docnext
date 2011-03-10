var drawing = false;
var start = {};
var img = new Image();
var nextImg = new Image();
var frames = [];

var order = 0;
var page = {
	number: 0,
	next: function() {
		this.number++;
	},
	prev: function() {
		this.number--;
		if (this.number <= -1)
			this.number = 0;
		order = 0;
		draw();
	},
	total: 0
};
var ctx;

img.src = "/dispatch/viewer/getPage?documentId=17&level=0&type=iPad&px=0&py=0&page=" + page.number;

window.addEventListener("load", function() {
	load();
	
	var can = document.getElementById("frame");
	ctx = document.getElementById("frame").getContext("2d");
	
	can.addEventListener("click", set_rect, true);
	can.addEventListener("mousemove", draw, true);
	can.addEventListener("mousemove", draw_editing, true);
	
	can.width = img.width;
	can.height = img.height;

	draw();
}, true);

function draw() {
	img.src = "/dispatch/viewer/getPage?documentId=17&level=0&type=iPad&px=0&py=0&page=" + page.number;

	img.onload = function(){
		draw_frame();
	}
	draw_frame();
}

function draw_frame(){
	// clear canvas
	ctx.clearRect(0, 0, 8000, 8000);
	ctx.drawImage(img, 0, 0);
	ctx.globalCompositeOperation = "darker";
	for (var i = 0; i < frames.length; i++) {
		if (frames[i].page == page.number) {
			ctx.fillStyle = "rgba(255, " + Math.abs(254 - (50 * frames[i].order % 255)) + ", 0, 1)";
			ctx.fillRect(frames[i].x, frames[i].y, frames[i].width, frames[i].height);
			ctx.strokeText(frames[i].order, frames[i].x, frames[i].y);
		}
	}
}

function draw_editing(e) {
	if (drawing) {
		ctx.restore();
		ctx.fillStyle = "rgba(255, 0, 0, 1)";

		var org = {};
		var width, height;

		if (start.x < e.offsetX) {
			org.x = start.x;
		} else {
			org.x = e.offsetX;
		}
		if (start.y < e.offsetY) {
			org.y = start.y;
		} else {
			org.y = e.offsetY;
		}
		
		width = Math.abs(start.x - e.offsetX);
		height = Math.abs(start.y - e.offsetY);
		ctx.save();
		ctx.fillRect(org.x, org.y, width, height);
	}
}

function set_rect(e) {
	var x = e.offsetX;
	var y = e.offsetY;
	
	if (!drawing) {
		start = {x:x, y:y};
	}
	
	if (drawing) {
		var org = {};
		var width, height;

		if (start.x < x) {
			org.x = start.x;
		} else {
			org.x = x;
		}
		if (start.y < y) {
			org.y = start.y;
		} else {
			org.y = y;
		}
		
		width = Math.abs(start.x - x);
		height = Math.abs(start.y - y);

		frames[frames.length] = {x:org.x, y:org.y, width:width, height:height, page:page.number, order:order++};
	}

	drawing = !drawing;
	save();
}

function clear_page() {
	for (var i = 0; i < frames.length; i++) {
		if (frames[i].page == page.number) {
			frames.splice(i, 1);
			i--;
		}
	}
	order = 0;
	save();
	draw();
}

function getInfo() {
  DocumentService.getInfo(id,
    function(data) {
      document.getElementById('title').value = data;
    }
  );
}

//
// persistence
//
var id;
function load(){
	id = readCookie('document_id');
	
	DocumentService.getFrames(id, function(data){
		frames = data;
		draw();
	});
}

function save() {
	DocumentService.setFrames(id, frames, function(){});	
}
