function extractMetadata(file)
{
   // Extract metadata - via repository action for now.
   // This should use the MetadataExtracter API to fetch properties, allowing for possible failures.
   var emAction = actions.create("extract-metadata");
   if (emAction != null)
   {
      // Call using readOnly = false, newTransaction = false
      emAction.execute(file, false, false);
   }
}

function exitUpload(statusCode, statusMsg)
{
   status.code = statusCode;
   status.message = statusMsg;
   status.redirect = true;
}

/**
 * Creates an new filename by adding a suffix to existing one in order to avoid duplicates in folder
 * The check that folder already contains the filename should be done before calling this function
 * @param filename - existing filename
 * @param destNode - folder node
 * @returns
 */
function createUniqueNameInFolder(filename, destNode)
{
   var counter = 1,
   tmpFilename,
   dotIndex,
   existingFile;
   do
   {
      dotIndex = filename.lastIndexOf(".");
      if (dotIndex == 0)
      {
         // File didn't have a proper 'name' instead it had just a suffix and started with a ".", create "1.txt"
         tmpFilename = counter + filename;
      }
      else if (dotIndex > 0)
      {
         // Filename contained ".", create "filename-1.txt"
         tmpFilename = filename.substring(0, dotIndex) + "-" + counter + filename.substring(dotIndex);
      }
      else
      {
         // Filename didn't contain a dot at all, create "filename-1"
         tmpFilename = filename + "-" + counter;
      }
      existingFile = destNode.childByNamePath(tmpFilename);
      counter++;
   } while (existingFile !== null);
   return tmpFilename;
}

/**
 * Determines if new filename looks like it was renamed as a result of the user's OS's duplicate file name avoidance.
 * This is particularly a problem for "Edit Offline" functionality where the user's workflow may require multiple
 * download, modification and upload cycles. See MNT-18018 for details.
 *
 * The de-duplication marker manifests itself as a " (1)" appended after the filename, before the extension.
 * @param fileName {string}
 * @param originalFileName {string}
 * @return {boolean}
 *
 * NOTE: duplicated in slingshot/source/web/components/upload/dnd-upload.js to allow client side detection too.
 */
function containsOSDeDupeMarkers(fileName, originalFileName) {
   // Components of RegEx:
   var deDupeMarker = "\\s\\([\\d]+\\)", // single space followed by one or more digits in literal brackets; e.g." (1)", " (2)", " (13)"
      fileExtension = "\\.[A-z\\d]+$"; // a single dot followed by one or more letters or numbers at end of string.  e.g. ".jpg", ".mp3", ".docx"

   // Extract file extension from originalFileName if it exists & remove from original file name.
   var regExMatch = new RegExp(fileExtension).exec(originalFileName),
      originalFileExtension = (regExMatch)? regExMatch[0] : "",
      originalFileNameWithoutExtension = (regExMatch) ? originalFileName.substr(0, regExMatch["index"]) : "";

   var combinedRegEx = new RegExp("^" + escapeRegExp(originalFileNameWithoutExtension) + deDupeMarker + escapeRegExp(originalFileExtension) + "$");

   return combinedRegEx.test(fileName);
}

/**
 * Make user input safe to pass into a RegEx
 *
 * @param string {string}
 * @return {string}
 */
function escapeRegExp(string) {
   return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function main()
{
   try
   {
      var filename = null,
         content = null,
         mimetype = null,
         siteId = null, site = null,
         containerId = null, container = null,
         destination = null,
         destNode = null,
         thumbnailNames = null,
         i;

      // Upload specific
      var uploadDirectory = null,
         createDirectory = false,
         contentType = null,
         aspects = [],
         overwrite = true; // If a filename clashes for a versionable file

      // Update specific
      var updateNodeRef = null,
         majorVersion = false,
         updateNameAndMimetype = false,
         description = "";
      
      // Prevents Flash- and IE8-sourced "null" values being set for those parameters where they are invalid.
      // Note: DON'T use a "!==" comparison for "null" here.
      var fnFieldValue = function(p_field)
      {
         return p_field.value.length() > 0 && p_field.value != "null" ? p_field.value : null;
      };

      // allow the locale to be set via an argument
      if (args["lang"] != null)
      {
         utils.setLocale(args["lang"]);
      }

      var uploadConfig = new XML(config.script),
          autoVersion = uploadConfig.autoVersion.toString() == "" ? null : uploadConfig.autoVersion.toString() == "true",
          autoVersionProps = uploadConfig.autoVersionProps.toString() == "" ? null : uploadConfig.autoVersionProps.toString() == "true";

      // Parse file attributes
      for each (field in formdata.fields)
      {
         switch (String(field.name).toLowerCase())
         {
            case "filename":
               filename = fnFieldValue(field);
               break;
            
            case "filedata":
               if (field.isFile)
               {
                  filename = filename ? filename : field.filename;
                  content = field.content;
                  mimetype = field.mimetype;
               }
               break;

            case "siteid":
               siteId = fnFieldValue(field);
               break;

            case "containerid":
               containerId = fnFieldValue(field);
               break;

            case "destination":
               destination = fnFieldValue(field);
               break;

            case "uploaddirectory":
               uploadDirectory = fnFieldValue(field);
               if ((uploadDirectory !== null) && (uploadDirectory.length() > 0))
               {
                  if (uploadDirectory.charAt(uploadDirectory.length() - 1) != "/")
                  {
                     uploadDirectory = uploadDirectory + "/";
                  }
                  // Remove any leading "/" from the uploadDirectory
                  if (uploadDirectory.charAt(0) == "/")
                  {
                     uploadDirectory = uploadDirectory.substr(1);
                  }
               }
               break;

            case "updatenoderef":
               updateNodeRef = fnFieldValue(field);
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
               
            case "updatenameandmimetype":
               updateNameAndMimetype = field.value == "true";
               break;
            
            case "createdirectory":
               createDirectory = field.value == "true";
               break;
         }
      }

      // MNT-7213 When alf_data runs out of disk space, Share uploads result in a success message, but the files do not appear
      if (formdata.fields.length == 0)
      {
         exitUpload(404, " No disk space available");
         return;
      }
	  
      // Ensure mandatory file attributes have been located. Need either destination, or site + container or updateNodeRef
      if ((filename === null || content === null) || (destination === null && (siteId === null || containerId === null) && updateNodeRef === null))
      {
         exitUpload(400, "Required parameters are missing");
         return;
      }

      /**
       * Site or Non-site?
       */
      if (siteId !== null && siteId.length() > 0)
      {
         /**
          * Site mode.
          * Need valid site and container. Try to create container if it doesn't exist.
          */
         site = siteService.getSiteInfo(siteId);
         if (site === null)
         {
            exitUpload(404, "Site (" + siteId + ") not found.");
            return;
         }

         container = site.getContainer(containerId);
         if (container === null)
         {
            try
            {
               // Create container since it didn't exist
               container = site.createContainer(containerId);
            }
            catch(e)
            {
               // Error could be that it already exists (was created exactly after our previous check) but also something else
               container = site.getContainer(containerId);
               if (container === null)
               {
                  // Container still doesn't exist, then re-throw error
                  throw e;
               }
               // Since the container now exists we can proceed as usual
            }
         }

         if (container === null)
         {
            exitUpload(404, "Component container (" + containerId + ") not found.");
            return;
         }
         
         destNode = container;
      }
      else if (destination !== null)
      {
         /**
          * Non-Site mode.
          * Need valid destination nodeRef.
          */
         destNode = search.findNode(destination);
         if (destNode === null)
         {
            exitUpload(404, "Destination (" + destination + ") not found.");
            return;
         }
      }

      /**
       * Update existing or Upload new?
       */
      if (updateNodeRef !== null)
      {
         /**
          * Update existing file specified in updateNodeRef
          */
         var updateNode = search.findNode(updateNodeRef);
         var workingcopy = updateNode.hasAspect("cm:workingcopy");

         if (updateNode === null)
         {
            exitUpload(404, "Node specified by updateNodeRef (" + updateNodeRef + ") not found.");
            return;
         }

         var newFilename = filename;
         if (updateNameAndMimetype)
         {
             //check to see if name is already used in folder
             var existingFile = updateNode.getParent().childByNamePath(filename);
             var existingFileNodeRef = (existingFile !== null) ? String(existingFile.nodeRef) : '',
            	 updateFileNodeRef = String(updateNodeRef);
             if (existingFile !== null && existingFileNodeRef !== updateFileNodeRef)
             {
                 //name it's already used for other than node to update; create a new one
                 newFilename = createUniqueNameInFolder(filename, updateNode.getParent());
             }
            // MNT-18018: Avoid getting stuck in a loop of renamed files when using the Edit Offline functionality
            // Only rename the file if it is not a working copy and it does not contain any OS de-duplication markers
            if (!(workingcopy && containsOSDeDupeMarkers(filename, updateNode.name)))
            {
               //update node name
               updateNode.setName(newFilename);
            }
         }

         if (!workingcopy && updateNode.isLocked)
         {
            // We cannot update a locked document (except working copy as per MNT-8736)
            exitUpload(404, "Cannot update locked document '" + updateNodeRef + "', supply a reference to its working copy instead.");
            return;
         }

         if (!workingcopy)
         {
            // Ensure the file is versionable (autoVersion and autoVersionProps read from config)
            if (autoVersion != null && autoVersionProps != null)
            {
                updateNode.ensureVersioningEnabled(autoVersion, autoVersionProps);
            }
            else
            {
                updateNode.ensureVersioningEnabled();
            }

            // It's not a working copy, do a check out to get the actual working copy
            updateNode = updateNode.checkoutForUpload();
         }

         // Update the working copy content
         updateNode.properties.content.write(content, updateNameAndMimetype, true, newFilename);
         // check it in again, with supplied version history note
         
         updateNode = updateNode.checkin(description, majorVersion);

         // Extract the metadata
         // (The overwrite policy controls which if any parts of
         //  the document's properties are updated from this)
         extractMetadata(updateNode);

         if (aspects.length != 0)
         {
            for (i = 0; i < aspects.length; i++)
            {
               if (!updateNode.hasAspect(aspects[i]))
               {
                  updateNode.addAspect(aspects[i]);
               }
            }
         }

         // Record the file details ready for generating the response
         model.document = updateNode;
      }
      else
      {
         /**
          * Upload new file to destNode (calculated earlier) + optional subdirectory
          */
         if (uploadDirectory !== null && uploadDirectory.length > 0)
         {
            var child = destNode.childByNamePath(uploadDirectory);
            if (child === null)
            {
               if (createDirectory)
               {
                  child = destNode.createFolderPath(uploadDirectory);
               }
               else
               {
                  exitUpload(404, "Cannot upload file since upload directory '" + uploadDirectory + "' does not exist.");
                  return;
               }
            }

            // MNT-12565
            while (child.isDocument)
            {
               if (child.parent === null)
               {
                  exitUpload(404, "Cannot upload file. You do not have permissions to access the parent folder for the document.");
                  return;
               }
               child = child.parent;
            }

            destNode = child;
         }

         /**
          * Existing file handling.
          */
         var existingFile = destNode.childByNamePath(filename);
         if (existingFile !== null)
         {
            // File already exists, decide what to do
            if (existingFile.hasAspect("cm:versionable") && overwrite)
            {
               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content, false, true);

               // Extract the metadata
               // (The overwrite policy controls which if any parts of
               //  the document's properties are updated from this)
               extractMetadata(existingFile);

               // Record the file details ready for generating the response
               model.document = existingFile;

               // MNT-8745 fix: Do not clean formdata temp files to allow for retries. Temp files will be deleted later when GC call DiskFileItem#finalize() method or by temp file cleaner.
               return;
            }
            else
            {
               // Upload component was configured to find a new unique name for clashing filenames
               filename = createUniqueNameInFolder(filename, destNode);
            }
         }

         /**
          * Create a new file.
          */
         var newFile;
         if (contentType !== null)
         {
            newFile = destNode.createFile(filename, contentType);
         }
         else
         {
            newFile = destNode.createFile(filename);
         }
         // Use the appropriate write() method so that the mimetype already guessed from the original filename is
         // maintained - as upload may have been via Flash - which always sends binary mimetype and would overwrite it.
         // Also perform the encoding guess step in the write() method to save an additional Writer operation.
         newFile.properties.content.write(content, false, true);
         newFile.save();
         

         // NOTE: Removal of first request for thumbnails to improve upload performance
         //       Thumbnails are still requested by Share on first render of the doclist image.

         // Additional aspects?
         if (aspects.length > 0)
         {
            for (i = 0; i < aspects.length; i++)
            {
               newFile.addAspect(aspects[i]);
            }
         }

         // Extract the metadata
         extractMetadata(newFile);

         // TODO (THOR-175) - review
         // Ensure the file is versionable (autoVersion and autoVersionProps read from config)
         if (autoVersion != null && autoVersionProps != null)
         {
             newFile.ensureVersioningEnabled(autoVersion, autoVersionProps);
         }
         else
         {
             newFile.ensureVersioningEnabled();
         }

         // Record the file details ready for generating the response
         model.document = newFile;
      }
      // MNT-8745 fix: Do not clean formdata temp files to allow for retries. Temp files will be deleted later when GC call DiskFileItem#finalize() method or by temp file cleaner.
   }
   catch (e)
   {
      // NOTE: Do not clean formdata temp files to allow for retries. It's possible for a temp file
      //       to remain if max retry attempts are made, but this is rare, so leave to usual temp
      //       file cleanup.
      
      // capture exception, annotate it accordingly and re-throw
      if (e.message && e.message.indexOf("AccessDeniedException") != -1)
      {
         e.code = 403;
      }
      else if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         e.code = 413;
      }
      else if (e.message && e.message.indexOf("org.alfresco.repo.content.ContentLimitViolationException") == 0)
      {
         e.code = 409;
      }
      else
      {
         e.code = 500;
         e.message = "Unexpected error occurred during upload of new content.";      
      }
      throw e;
   }
}

main();
