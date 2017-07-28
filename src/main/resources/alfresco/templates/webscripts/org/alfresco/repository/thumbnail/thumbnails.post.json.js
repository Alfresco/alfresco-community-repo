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
   var thumbnailName = null;
   if (json.isNull("thumbnailName") == false)
   {
      thumbnailName = json.get("thumbnailName");
   }
   
   // 400 if no thumbnail name found
   if (thumbnailName == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Thumbnail name was not provided");
      return;
   }  
   
   // TODO double check that the thumbnail name is valid
   
   // Get the flag indicating whether to create the thumbnail asynchronously or not
   var async = false;
   var asyncString = args.as;
   if (asyncString != null)
   {
      async = utils.toBoolean(asyncString);
   }
   
   if (async == false)
   {
	   // Create the thumbnail
	   var thumbnail = node.createThumbnail(thumbnailName);
	   
	   // Prep the model
	   model.node = node;
	   model.thumbnailName = thumbnailName;
	   model.thumbnail = thumbnail;
   }
   else
   {
	   // Create the the thumbnail asyncronously
	   node.createThumbnail(thumbnailName, true);
   }    
}

main();