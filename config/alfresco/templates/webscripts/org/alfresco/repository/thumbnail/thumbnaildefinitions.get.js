function main()
{
   // Get the node from the URL
   var pathSegments = url.match.split("/");
   var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
   var node = search.findNode(pathSegments[2], reference);
 
   // 404 if the node to thumbnail is not found
   if (node == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "The node could not be found");
	  return;
   }
   
   // Get the thumbnail definitions
   var thumbnailDefinitions = node.getThumbnailDefinitions();
   
   // Add them to the model
   model.node = node;
   model.thumbnailDefinitions = thumbnailDefinitions; 
}

main();
