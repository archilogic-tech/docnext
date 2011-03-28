var id;

$(document).ready(function(){
  id = readCookie("document_id");
   
  DocumentService.getTitle(id, function(data) {
      $("#title").val(data);
    });
  DocumentService.getPublisher(id, function(data) {
      $("#publisher").val(data);
    });
  DocumentService.getBinding(id, function(data) {
      $("#binding_left").attr("checked", data == "left");
      $("#binding_right").attr("checked", data == "right");
    });
  DocumentService.getFlow(id, function(data) {
      $("#flow_left").attr("checked", data == "left");
      $("#flow_right").attr("checked", data == "right");
    });
  
  $("input").change(save);
  //$("input").keydown(save);
});
  
function save() {
  DocumentService.setTitle(id, $("#title").val(), function(data) {});
  //DocumentService.setTitle(id, "養女", function(data) {});
  DocumentService.setPublisher(id, $("#publisher").val(), function(data) {});
  
  var binding = "none";
  if ($("#binding_left").attr("checked"))
    binding = "left";
  if ($("#binding_right").attr("checked"))
    binding = "right";
  DocumentService.setBinding(id, binding, function() {});

  var flow = "none";
  if ($("#flow_left").attr("checked"))
    flow = "left";
  if ($("#flow_right").attr("checked"))
    flow = "right";
  DocumentService.setFlow(id, flow, function() {});
}
