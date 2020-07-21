var filename = null;
var content = null;
var title = "";
var description = "";

// locate file attributes
for each (field in formdata.fields)
{
  if (field.name == "title")
  {
    title = field.value;
  }
  else if (field.name == "desc")
  {
    description = field.value;
  }
  else if (field.name == "file" && field.isFile)
  {
    filename = field.filename;
    content = field.content;
  }
}

// ensure mandatory file attributes have been located
if (filename == undefined || content == undefined)
{
  status.code = 400;
  status.message = "Uploaded file cannot be located in request";
  status.redirect = true;
}
else
{
  // create document in company home for uploaded file
  upload = companyhome.createFile("upload" + companyhome.children.length + "_" + filename) ;
  upload.properties.content.write(content);
  upload.properties.content.mimetype = "UTF-8";
  upload.properties.title = title;
  upload.properties.description = description;
  upload.save();
  
  // setup model for response template
  model.upload = upload;
}