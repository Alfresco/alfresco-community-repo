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
   
   // Get the thumbnail name from the JSON content 
   var thumbnailName = url.templateArgs.thumbnailname; //pathSegments[pathSegments.length - 1];
   
   // 404 if no thumbnail name found
   if (thumbnailName == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Thumbnail name was not provided");
      return;
   }  
   
   // Get the thumbnail
   var thumbnail = node.getThumbnail(thumbnailName);
   if (thumbnail == null)
   {
      // 404 since no thumbnail was found
      status.setCode(status.STATUS_NOT_FOUND, "Thumbnail was not found");
   }
   
   // Place the details of the thumbnail into the model, this will be used to stream the content to the client
   model.contentNode = thumbnail; 
}

main();