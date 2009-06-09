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
   
   var currentType = utils.shortQName(node.type),
      selectableTypes = [];

   var myConfig = new XML(config.script),
      xmlType = myConfig..type.(@name == currentType);
   
   // Found match?
   if (xmlType.@name == currentType)
   {
      for each(var xmlSubtype in xmlType.subtype)
      {
         selectableTypes.push(xmlSubtype.@name.toString());
      }
   }

   model.currentType = currentType;
   model.selectableTypes = selectableTypes;
}

main();