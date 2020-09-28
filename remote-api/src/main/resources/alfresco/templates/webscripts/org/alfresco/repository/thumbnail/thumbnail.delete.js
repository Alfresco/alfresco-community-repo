function main()
{
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
   
   // Get the thumbnail
   var thumbnail = node.getThumbnail(thumbnailName);
   if (thumbnail == null || thumbnail.size == 0)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Thumbnail was not found");
      return;
   }

   // Delete the asset
   if (!thumbnail.remove())
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not delete");
      return;
   }
}

main();