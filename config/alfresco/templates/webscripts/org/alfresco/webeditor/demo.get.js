function main()
{
   var storeType = url.templateArgs["store_type"];
   var storeId = url.templateArgs["store_id"];
   var nodeId = url.templateArgs["id"];
      
   if (logger.isLoggingEnabled())
   {
       logger.log("Rendering AWE demo content for node: " + storeType + "://" + storeId + "/" + nodeId);
   }
   
   var nodeRef = storeType + "://" + storeId + "/" + nodeId;
   var node = search.findNode(nodeRef);
   if (node != null)
   {
      model.title = node.properties.title;
      model.caption = node.properties.description;
      model.text = node.content;
   }
   else
   {
      model.title = "";
      model.caption = "";
      model.text = "Node not found!";
   }
}

main();