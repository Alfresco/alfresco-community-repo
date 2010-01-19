function main()
{
   try
   {
      var filename = null,
         content = null,
         mimetype = null,
         siteId = null,
         containerId = null,
         thumbnailNames = null,
         i;

      // Upload specific
      var uploadDirectory = null,
         title = "",
         contentType = null,
         aspects = [],
         overwrite = true; // If a filename clashes for a versionable file

      // Update specific
      var updateNodeRef = null,
         majorVersion = false,
         description = "";

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

            case "aspects":
               aspects = field.value != "-" ? field.value.split(",") : [];
               break;

            case "majorversion":
               majorVersion = field.value == "true";
               break;

            case "overwrite":
               overwrite = field.value == "true";
               break;

            case "thumbnails":
               thumbnailNames = field.value;
               break;
         }
      }

      // Ensure mandatory file attributes have been located
      if (siteId === null || containerId === null || filename === null || content === null)
      {
         status.code = 400;
         status.message = "Required parameters are missing";
         status.redirect = true;
         return;
      }

      var site = siteService.getSite(siteId);
      if (site === null)
      {
         status.code = 404;
         status.message = "Site (" + siteId + ") not found.";
         status.redirect = true;
         return;
      }

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

      if (updateNodeRef !== null && updateNodeRef != "" && (uploadDirectory === null || uploadDirectory == ""))
      {
         // Update existing file mode
         var workingCopy = search.findNode(updateNodeRef);
         if (workingCopy.isLocked)
         {
            // We cannot update a locked document
            status.code = 404;
            status.message = "Cannot update locked document '" + updateNodeRef + "', supply a reference to its working copy instead.";
            status.redirect = true;
            return;
         }

         if (!workingCopy.hasAspect("cm:workingcopy"))
         {
            // Ensure the original file is versionable - may have been uploaded via different route
            if (!workingCopy.hasAspect("cm:versionable"))
            {
               // Ensure the file is versionable
               var props = new Array(1);
	            props["cm:autoVersionOnUpdateProps"] = false;
               workingCopy.addAspect("cm:versionable", props);
            }

            if (workingCopy.versionHistory == null)
            {
               // Create the first version manually so we have 1.0 before checkout
               workingCopy.createVersion("", true);
            }

            // It's not a working copy, do a check out to get the actual working copy
            workingCopy = workingCopy.checkout();
         }

         // Update the working copy content
         workingCopy.properties.content.write(content);
         // Reset working copy mimetype and encoding
         workingCopy.properties.content.guessMimetype(filename);
         workingCopy.properties.content.encoding = "UTF-8";
         // check it in again, with supplied version history note
         workingCopy = workingCopy.checkin(description, majorVersion);

         model.document = workingCopy;
      }
      else if (uploadDirectory !== null && (updateNodeRef === null || updateNodeRef == ""))
      {
         // Upload file mode
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
            return;
         }

         var existingFile = container.childByNamePath(uploadDirectory + filename);
         if (existingFile !== null)
         {
            // File already exists, decide what to do
            if (existingFile.hasAspect("cm:versionable") && overwrite)
            {
               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content);
               model.document = existingFile;

               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content);

               // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
               existingFile.properties.content.guessMimetype(filename);
               existingFile.properties.content.encoding = "UTF-8";
               existingFile.save();

               model.document = existingFile;
               return;
            }
            else
            {
               // Upload component was configured to find a new unique name for clashing filenames
               var suffix = 1;
               var tmpFilename;
               while (existingFile !== null)
               {
                  if(filename.lastIndexOf(".") > 0)
                  {
                     tmpFilename = filename.substring(0, filename.lastIndexOf(".")) + "-" + suffix + filename.substring(filename.lastIndexOf("."));
                  }
                  else
                  {
                     tmpFilename = filename + "-" + suffix;
                  }
                  existingFile = container.childByNamePath(uploadDirectory + tmpFilename);
                  suffix++;
               }
               filename = tmpFilename;
            }
         }

         // save the new file (original or renamed file) as long as an overwrite hasn't been performed
         var newFile = destNode.createFile(filename);
         if (contentType !== null)
         {
            newFile.specializeType(contentType);
         }
         newFile.properties.content.write(content);

         // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
         newFile.properties.content.guessMimetype(filename);
         newFile.properties.content.encoding = "UTF-8";
         newFile.save();

         // Additional aspects?
         if (aspects.length > 0)
         {
            for (i = 0; i < aspects.length; i++)
            {
               newFile.addAspect(aspects[i]);
            }
         }

         // Create thumbnail?
         if (thumbnailNames != null)
         {
            var thumbnails = thumbnailNames.split(",");
            for (i = 0; i < thumbnails.length; i++)
            {
               var thumbnailName = thumbnails[i];
               if (thumbnailName != "" && thumbnailService.isThumbnailNameRegistered(thumbnailName))
               {
                  newFile.createThumbnail(thumbnailName, true);
               }
            }
         }

         // Extract metadata - via repo action for now
         // This should use the MetadataExtracter API to fetch properties, allowing
         // for possible failures.
         var emAction = actions.create("extract-metadata");
         if (emAction != null)
         {
            // Call using readOnly = false, newTransaction = false
            emAction.execute(newFile, false, false);
         }

         // Set the title if none set during meta-data extract
         newFile.reset();
         if (newFile.properties.title == null)
         {
            newFile.properties.title = title;
            newFile.save();
         }

         model.document = newFile;
      }
      else
      {
         status.code = 404;
         status.message = "Illegal arguments: updateNodeRef OR uploadDirectory must be provided (not both)";
         status.redirect = true;
         return;
      }
   }
   catch (e)
   {
      var x = e;
      status.code = 500;
      status.message = "Unexpected error occured during upload of new content.";
      if(x.message && x.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         status.code = 413;
         status.message = x.message;
      }
      status.redirect = true;
      return;
   }
}

main();