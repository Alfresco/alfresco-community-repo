function main()
{
   // nodeRef input
   var storeType = url.templateArgs.store_type,
      storeId = url.templateArgs.store_id,
      id = url.templateArgs.id,
      nodeRef = storeType + "://" + storeId + "/" + id;
   
   var node = search.findNode(nodeRef);
   if (node === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
      return null;
   }
   
   var current = [],
      currentSet = node.aspectsSet.toArray();
   
   for (index in currentSet)
   {
      current.push(currentSet[index].toString());
   }

   return (
   {
      current: current
   });
}

model.aspects = main();