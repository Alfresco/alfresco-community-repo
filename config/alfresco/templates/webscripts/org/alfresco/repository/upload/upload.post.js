var filename = null;
var content = null;
var mimetype = null;
var siteId = null;
var containerId = null;
var thumbnailName = null;

// Upload specific
var uploadDirectory = null;
var title = "";
var overwrite = false;

// Update specific
var updateNodeRef = null;
var majorVersion = false;
var description = "";

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
            mimetype = field.mimetype;
         }
         break;
      
      case "siteid":
         siteId = field.value;
         break;
         
      case "containerid":
         containerId = field.value;
         break;
      
      case "uploaddirectory":
         uploadDirectory = field.value;
         // Remove any leading "/" from the uploadDirectory
         if (uploadDirectory.substr(0, 1) == "/")
         {
            uploadDirectory = uploadDirectory.substr(1);
         }
         // Ensure uploadDirectory ends with "/" if not the root folder
         if ((uploadDirectory.length > 0) && (uploadDirectory.substring(uploadDirectory.length - 1) != "/"))
         {
            uploadDirectory = uploadDirectory + "/";
         }
         break;

      case "updatenoderef":
         updateNodeRef = field.value;
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

      case "majorversion":
         majorVersion = field.value == "true";
         break;

      case "overwrite":
         overwrite = field.value == "true";
         break;
      
      case "thumbnail":
         thumbnailName = field.value;
         break;
   }
}

// Ensure mandatory file attributes have been located
if (siteId === null || containerId === null || filename === null || content === null)
{
   status.code = 400;
   status.message = "Required parameters are missing";
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
      // Upload mode, since uploadDirectory was used
      var container = site.getContainer(containerId);
      if (container === null)
      {
         container = site.createContainer(containerId);
      }
      
      if (container === null)
      {
         status.code = 404;
         status.message = "Component container (" + containerId + ") not found.";
         status.redirect = true;
      }
      
      if (updateNodeRef !== null && uploadDirectory === null)
      {
         // Update mode, since updateNodeRef was used
         var workingCopy = search.findNode(updateNodeRef);
         if (workingCopy.isLocked)
         {
            // It's not a working copy, should have been the working copy, throw error
            status.code = 404;
            status.message = "Cannot upload document since updateNodeRef '" + updateNodeRef + "' points to a locked document, supply a nodeRef to its working copy instead.";
            status.redirect = true;
         }
         else if (!workingCopy.hasAspect("cm:workingcopy"))
         {
            // It's not a working copy, do a check out to get the working copy
            workingCopy = workingCopy.checkout();
         }
         // Update the working copy
         workingCopy.properties.content.write(content);
         // check it in again, but with a version history note and as minor or major version increment
         workingCopy = workingCopy.checkin(description, majorVersion);
         model.document = workingCopy;
      }
      else if (uploadDirectory !== null && updateNodeRef === null)
      {
         var destNode = container;
         if (uploadDirectory != "")
         {
            destNode = container.childByNamePath(uploadDirectory);
         }
         if (destNode === null)
         {
            status.code = 404;
            status.message = "Cannot upload file since uploadDirectory '" + uploadDirectory + "' does not exist.";
            status.redirect = true;
         }

         var existingFile = container.childByNamePath(uploadDirectory + filename);
         var overwritten = false;
         if (existingFile !== null)
         {
            // File already exists, decide what to do
            if (overwrite)
            {
               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content);
               model.document = existingFile;
               // Stop creation of new file below
               overwritten = true;
            }
            else
            {
               // Upload component was configured to find a new unique name for clashing filenames
               var suffix = 1;
               var tmpFilename;
               while(existingFile !== null)
               {
                  tmpFilename = filename.substring(0, filename.lastIndexOf(".")) + "-" + suffix + filename.substring(filename.lastIndexOf("."));
                  existingFile = container.childByNamePath(uploadDirectory + tmpFilename);
                  suffix++;
               }
               filename = tmpFilename;
            }
         }

         // save the new file (original or renamed file) as long as an overwrite hasn't been performed
         if (!overwritten)
         {
            var newFile = destNode.createFile(filename);
            newFile.properties.contentType = contentType;
            newFile.properties.content.write(content);
            // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
            newFile.properties.content.guessMimetype(filename);
            newFile.properties.content.encoding = "UTF-8";
            newFile.properties.title = title;
            newFile.properties.description = description;
            // Make file versionable (todo: check that this is ok depending on version store development)
            newFile.addAspect("cm:versionable");
            // Save new file
            newFile.save();
            // Create thumbnail?
            if (thumbnailName && thumbnailService.isThumbnailNameRegistered(thumbnailName))
            {
               newFile.createThumbnail(thumbnailName, true);
            }
            model.document = newFile;
         }
      }
      else
      {
         status.code = 404;
         status.message = "Illegal arguments: updateNodeRef OR uploadDirectory must be provided (not both)";
         status.redirect = true;
      }
   }
}