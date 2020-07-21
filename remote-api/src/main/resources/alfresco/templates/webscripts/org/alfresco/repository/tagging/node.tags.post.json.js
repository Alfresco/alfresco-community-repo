function main()
{
   // Get the node from the URL
   var pathSegments = url.match.split("/");
   var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
   var node = search.findNode(pathSegments[2], reference);
   
   // 404 if the node is not found
   if (node == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "The node could not be found");
      return;
   }
   
   // Get the array of posted tags
   for (var index = 0; index < json.length(); index++)
   {
      node.addTag(json.getString(index));
   }
   
   // save the node
   node.save();
   
   // Get the tags of the node
   model.tags = node.tags;
}

main();