/**
 * Note!
 *
 * If an error occurr make sure to set redirect to false,
 * otherwise errors will not be displayed in the flash multi upload.
 */
var filename = null;
var content = null;
var siteId = null;
var path = null;
var title = "";
var description = "";
var contentType = "Content";
var version = null;

// locate file attributes
for each (field in formdata.fields)
{
   if (field.name == "filedata" && field.isFile)
   {
      filename = field.filename;
      content = field.content;
   }
   else if (field.name == "siteId")
   {
      siteId = field.value;
   }
   else if (field.name == "path")
   {
      // todo: Use this when the doclist is finished
      //path = field.value;
      path = "/Company Home" + field.value;
   }
   else if (field.name == "filename")
   {
      title = field.value;
   }
   else if (field.name == "description")
   {
      description = field.value;
   }
   else if (field.name == "contentType")
   {
      contentType = field.value;
   }
   else if (field.name == "version")
   {
      version = field.value;
   }
}

// ensure mandatory file attributes have been located
if (siteId == undefined || path == undefined || filename == undefined || content == undefined)
{
   status.code = 400;
   status.message = "Uploaded file cannot be located in request";
   status.redirect = false;
}
else
{
   var destination = roothome.childByNamePath(path);
   if(destination !== undefined)
   {
      if(destination.isDocument)
      {
         // Update content with correct version
         model.upload = destination;
      }
      else
      {
         var tmp = roothome.childByNamePath(path + "/" + filename);
         if(tmp)
         {
            // This shall result in a rename of the new file instead of an error probably
            status.code = 400;
            status.message = "File " + filename + "already exists in folder " + path;
            status.redirect = false;
         }
         else
         {
            // create new content with correct mimetype
            upload = destination.createFile(filename) ;
            upload.properties.contentType = contentType;
            upload.properties.content.write(content);
            upload.properties.content.mimetype = "UTF-8";
            upload.properties.title = title;
            upload.properties.description = description;
            upload.save();

            model.upload = upload;            
         }
      }
   }
   else
   {
      status.code = 400;
      status.message = "Cannot upload file since path '" + path + "' doesn't exist.";
      status.redirect = false;
   }

}