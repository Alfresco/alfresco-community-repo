var _uploads = [];

function handle_upload_helper(fileInputElement,
                              uploadId,
                              callback,
                              contextPath,
                              actionUrl,
                              params)
{
  var id = fileInputElement.getAttribute("name");
  var d = fileInputElement.ownerDocument;
  var w = d.defaultView || d.parentWindow;
  var iframe = d.createElement("iframe");
  iframe.style.display = "none";
  iframe.name = id + "upload_frame";
  iframe.id = iframe.name;
  d.body.appendChild(iframe);

  // makes it possible to target the frame properly in ie.
  w.frames[iframe.name].name = iframe.name;

  _uploads[uploadId] = { path: fileInputElement.value, callback: callback };

  var form = d.createElement("form");
  d.body.appendChild(form);
  form.id = id + "_upload_form";
  form.name = form.id;
  form.style.display = "none";
  form.method = "post";
  form.encoding = "multipart/form-data";
  form.enctype = "multipart/form-data";
  form.target = iframe.name;
  if (actionUrl != undefined && actionUrl != null)
  {
    actionUrl = contextPath + actionUrl;
  }
  else
  {
    actionUrl = contextPath + "/uploadFileServlet"
  }
  form.action = actionUrl;
  form.appendChild(fileInputElement);

  var id = d.createElement("input");
  id.type = "hidden";
  form.appendChild(id);
  id.name = "upload-id";
  id.value = uploadId;

  for (var i in params)
  {
    var p = d.createElement("input");
    p.type = "hidden";
    form.appendChild(p);
    id.name = i;
    id.value = params[i];
  }

  var rp = d.createElement("input");
  rp.type = "hidden";
  form.appendChild(rp);
  rp.name = "return-page";
  if (w != window)
  {
    w.upload_complete_helper = window.upload_complete_helper;
  }

  rp.value = "{id: '" + uploadId + "', args: {error: '${_UPLOAD_ERROR}', fileTypeImage: '${_FILE_TYPE_IMAGE}'}}";

  form.submit();
}

function upload_complete_helper(id, args)
{
  var upload = _uploads[id];
  upload.callback(id, 
                  upload.path, 
                  upload.path.replace(/.*[\/\\]([^\/\\]+)/, "$1"),
                  args.fileTypeImage,
                  args.error != "${_UPLOAD_ERROR}" ? args.error : null);
}
