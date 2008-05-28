/**
 * Note!
 *
 * If an error occurr make sure to set redirect to false,
 * otherwise errors will not be displayed in the flash multi upload.
 */
var filename = null;
var content = null;
var siteId = null;
var componentId = null;
var path = null;
var title = "";
var description = "";
var contentType = "Content";
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
         
      case "componentid":
         componentId = field.value;
         break;
      
      case "path":
         path = field.value;
         if (path == "")
         {
            path = "/";
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
if (siteId === null || componentId === null || path === null || filename === null || content === null)
{
   status.code = 400;
   status.message = "Uploaded file cannot be located in request";
   status.redirect = false;
}
else
{
   var site = siteService.getSite(siteId);
   if (site === null)
   {
      status.code = 400;
      status.message = "Site (" + siteId + ") not found.";
      status.redirect = false;
   }
   else
   {
      var container = site.getContainer(componentId);
      if (container === null)
      {
         status.code = 400;
         status.message = "Site container (" + containerId + ") not found.";
         status.redirect = false;
      }
      else
      {
         var filepath = path + (path.substring(path.length() - 1) == "/" ? "" : "/") + filename;
         var existsFile = container.childByNamePath(filepath);
         if (existsFile !== null)
         {
            // TODO: Update existing file
            status.code = 400;
            status.message = "File " + filename + "already exists in folder " + path;
            status.redirect = false;
         }
         else
         {
            if (path.substring(0, 1) == "/")
            {
               path = path.substring(1);
               filepath = filepath.substring(1);
            }
            var existsPath = container.childByNamePath(path);
            if ((existsPath === null) && (path != ""))
            {
               status.code = 400;
               status.message = "Cannot upload file since path '" + path + "' does not exist.";
               status.redirect = false;
            }
            else
            {
               // Create new content with correct mimetype
               upload = container.createFile(filepath) ;
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
   }
}