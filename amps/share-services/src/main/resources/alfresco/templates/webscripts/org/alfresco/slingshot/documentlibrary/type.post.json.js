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
   
   if (!json.has("type"))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "'type' parameter not supplied");
      return null;
   }
   
   var type = json.get("type");
   
   if (!node.specializeType(type))
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not change type of nodeRef '" + nodeRef + "' to '" + type + "'");
      return null;
   }

   model.currentType = type;
}

main();