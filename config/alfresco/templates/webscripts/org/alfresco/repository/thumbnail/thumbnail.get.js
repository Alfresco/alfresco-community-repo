function main()
{
   // Indicate whether or not the thumbnail can be cached by the browser. Caching is allowed if the lastModified
   // argument is provided as this is an indication of request uniqueness and therefore the browser will have
   // the latest thumbnail image.
   if (args.lastModified != null)
   {
      model.allowBrowserToCache = "true";
   }
   else
   {
      model.allowBrowserToCache = "false";
   }
   
   // Get the node from the URL
   var pathSegments = url.match.split("/");
   var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
   var node = search.findNode(pathSegments[2], reference);
   
   // 404 if the node to thumbnail is not found
   if (node == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "The thumbnail source node could not be found");
      return;
   }

   // 400 if the node is not a subtype of cm:content
   if (!node.isSubType("cm:content"))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "The thumbnail source node is not a subtype of cm:content");
      return;
   }
   
   // Get the thumbnail name from the JSON content 
   var thumbnailName = url.templateArgs.thumbnailname; 
   
   // 404 if no thumbnail name found
   if (thumbnailName == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Thumbnail name was not provided");
      return;
   }
   
   // Get the queue/force create setting
   var qc = false;
   var fc = false;
   if (args.c != null)
   {
      if (args.c == "queue")
      {
         qc = true;
      }
      else if (args.c == "force")
      {
         fc = true;
      }
   }
   
   // Get the place holder flag
   var ph = false;
   var phString = args.ph;
   if (phString != null)
   {
      ph = utils.toBoolean(phString);
   }
   
   // Get the thumbnail
   var thumbnail = node.getThumbnail(thumbnailName);
   if (thumbnail == null || thumbnail.size == 0)
   {
      // Remove broken thumbnail
      if (thumbnail != null)
      {
         thumbnail.remove();
      }
      
      // Queue the creation of the thumbnail if appropriate
      if (fc)
      {
        model.contentNode = node.createThumbnail(thumbnailName, false);
      }
      else
      {
         if (qc)
         {
            node.createThumbnail(thumbnailName, true);
         }
         
         if (ph == true)
         {
            // Try and get the place holder resource. We use a method in the thumbnail service
            // that by default gives us a resource based on the content's mime type.
            var phPath = thumbnailService.getMimeAwarePlaceHolderResourcePath(thumbnailName, node.mimetype);
            if (phPath == null)
            {
               // 404 since no thumbnail was found
               status.setCode(status.STATUS_NOT_FOUND, "Thumbnail was not found and no place holder resource set for '" + thumbnailName + "'");
               return;
            }
            else
            {
               // Set the resouce path in the model ready for the content stream to send back to the client
               model.contentPath = phPath;
            }
         }
         else
         {
            // 404 since no thumbnail was found
            status.setCode(status.STATUS_NOT_FOUND, "Thumbnail was not found");
            return;
         }
      }
   }
   else
   {
      // Place the details of the thumbnail into the model, this will be used to stream the content to the client
      model.contentNode = thumbnail;
   } 
}

main();