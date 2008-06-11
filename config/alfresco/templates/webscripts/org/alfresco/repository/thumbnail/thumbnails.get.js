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
   
   // Get the thumbnails
   var thumbnails = node.getThumbnails();
   
   // Add them to the model
   model.node = node;
   model.thumbnails = thumbnails; 
}

main();