load();
function load() {
  var id = readCookie('document_id');
   
  DocumentService.getTitle(id,
    function(data) {
      document.getElementById('title').value = data;
    }
  );
  DocumentService.getPublisher(id,
    function(data) {
      document.getElementById('publisher').value = data;
    }
  );
  DocumentService.getBinding(id,
    function(data) {
      if (data == 'left')
        $('#binding_left')[0].checked = true;
      else if (data == 'right')
        $('#binding_right')[0].checked = true;
    }
  );
  DocumentService.getFlow(id,
    function(data) {
      if (data == 'left')
        $('#flow_left')[0].checked = true;
      else if (data == 'right')
        $('#flow_right')[0].checked = true;
    }
  );
}
  
function save() {
  var id = readCookie('document_id');

  var title = document.getElementById('title').value;
  DocumentService.setTitle(id, title, function(data) {});

  var publisher = document.getElementById('publisher').value;
  DocumentService.setPublisher(id, publisher, function(data) {});
  
  var binding = "none";
  if ($("#binding_left")[0].checked)
    binding = "left";
  if ($("#binding_right")[0].checked)
    binding = "right";
  DocumentService.setBinding(id, binding, function() {});

  var flow = "none";
  if ($("#flow_left")[0].checked)
    flow = "left";
  if ($("#flow_right")[0].checked)
    flow = "right";
  DocumentService.setFlow(id, flow, function() {});
}
