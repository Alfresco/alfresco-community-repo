function main()
{
   if (url.templateArgs.store_type === null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "NodeRef missing");
      return;
   }

   // nodeRef input
   var storeType = url.templateArgs.store_type,
      storeId = url.templateArgs.store_id,
      id = url.templateArgs.id,
      nodeRef = storeType + "://" + storeId + "/" + id,
      node = search.findNode(nodeRef);
   
   if (node === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
      return null;
   }

   model.node = node;
   
   if (node.parent !== null && node.parent.hasPermission("ReadProperties"))
   {
      model.parent = node.parent;
   }
}

main();