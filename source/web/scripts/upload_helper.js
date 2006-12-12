var _uploads = [];

function handle_upload_helper(fileInputElement,
                              uploadId,
                              callback,
                              contextPath)
{
  var id = fileInputElement.getAttribute("name");
  var d = fileInputElement.ownerDocument;
  var iframe = d.createElement("iframe");
  iframe.style.display = "none";
  iframe.name = id + "upload_frame";
  iframe.id = iframe.name;
  document.body.appendChild(iframe);

  // makes it possible to target the frame properly in ie.
  window.frames[iframe.name].name = iframe.name;

  _uploads[uploadId] = { path: fileInputElement.value, callback: callback };

  var form = d.createElement("form");
  d.body.appendChild(form);
  form.style.display = "none";
  form.method = "post";
  form.encoding = "multipart/form-data";
  form.enctype = "multipart/form-data";
  form.target = iframe.name;
  form.action = contextPath + "/uploadFileServlet";
  form.appendChild(fileInputElement.cloneNode(true));

  var id = document.createElement("input");
  form.appendChild(id);
  id.type = "hidden";
  id.name = "upload-id";
  id.value = uploadId;

  var rp = document.createElement("input");
  form.appendChild(rp);
  rp.type = "hidden";
  rp.name = "return-page";
  rp.value = "javascript:window.parent.upload_complete_helper('" + uploadId + "')";

  form.submit();
}

function upload_complete_helper(id)
{
  var upload = _uploads[id];
  upload.callback(id, upload.path);
}
