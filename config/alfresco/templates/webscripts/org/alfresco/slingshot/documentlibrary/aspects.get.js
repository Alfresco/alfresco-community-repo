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
      visible = [],
      addable = [],
      removeable = [],
      aspect;

   var currentSet = node.aspectsSet.toArray();
   for (index in currentSet)
   {
      current.push(currentSet[index].toString());
   }

   var myConfig = new XML(config.script);
   
   for each (name in myConfig.visible.aspect.@name)
   {
      visible.push(name.toString());
   }

   for each (name in myConfig.addable.aspect.@name)
   {
      addable.push(name.toString());
   }

   for each (name in myConfig.removeable.aspect.@name)
   {
      removeable.push(name.toString());
   }

   return (
   {
      current: current,
      visible: visible,
      addable: addable,
      removeable: removeable
   });
}

model.aspects = main();