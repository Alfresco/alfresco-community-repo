var filename = null;
var content = null;
var siteId = null;
var containerId = null;
var path = null;
var title = "";
var description = "";
var version = null;

// Parse file attributes
for each (field in formdata.fields)
{
   switch (String(field.name).toLowerCase())
   {
      case "filedata":
         if (field.isFile)
         {
            filename = field.filename;
            content = field.content;
         }
         break;
      
      case "siteid":
         siteId = field.value;
         break;
         
      case "containerid":
         containerId = field.value;
         break;
      
      case "path":
         path = field.value;
         // Remove any leading "/" from the path
         if (path.substr(0, 1) == "/")
         {
            path = path.substr(1);
         }
         // Ensure path ends with "/" if not the root folder
         if ((path.length > 0) && (path.substring(path.length - 1) != "/"))
         {
            path = path + "/";
         }
         break;

      case "filename":
         title = field.value;
         break;

      case "description":
         description = field.value;
         break;

      case "contenttype":
         contentType = field.value;
         break;

      case "version":
         version = field.value;
         break;
   }
}

// Ensure mandatory file attributes have been located
if (siteId === null || containerId === null || path === null || filename === null || content === null)
{
   status.code = 400;
   status.message = "Uploaded file cannot be located in request";
   //status.redirect = false;
}
else
{
   var site = siteService.getSite(siteId);
   if (site === null)
   {
      status.code = 404;
      status.message = "Site (" + siteId + ") not found.";
      status.redirect = true;
   }
   else
   {
      var container = site.getContainer(containerId);
      if (container === null)
      {
         status.code = 404;
         status.message = "Component container (" + containerId + ") not found.";
         status.redirect = true;
      }
      else
      {
         var filepath = path + filename;
         var existsFile = container.childByNamePath(filepath);
         if (existsFile !== null)
         {
            // TODO: what should happen?
            status.code = 400;
            status.message = "File " + filename + "already exists in folder " + path;
            status.redirect = true;
         }
         else
         {
            var destNode = container;
            if (path != "")
            {
               destNode = container.childByNamePath(path);
            }
            if (destNode === null)
            {
               status.code = 404;
               status.message = "Cannot upload file since path '" + path + "' does not exist.";
               status.redirect = true;
            }
            else
            {
               upload = destNode.createFile(filename) ;
               upload.properties.contentType = contentType;
               upload.properties.content.write(content);
               // reapply mimetype as upload may have been via Flash - which always sends binary mimetype
               upload.properties.content.guessMimetype(filename);
               upload.properties.content.encoding = "UTF-8";
               upload.properties.title = title;
               upload.properties.description = description;
               upload.save();

               model.upload = upload;            
            }
         }
      }
   }
}