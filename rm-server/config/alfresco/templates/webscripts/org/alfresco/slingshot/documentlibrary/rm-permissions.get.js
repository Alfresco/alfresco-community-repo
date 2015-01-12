<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/permissions.get.js">

function getRmPermissions()
{
   /**
    * nodeRef input: store_type, store_id and id
    */
   var storeType = url.templateArgs.store_type,
      storeId = url.templateArgs.store_id,
      id = url.templateArgs.id,
      nodeRef = storeType + "://" + storeId + "/" + id,
      node = ParseArgs.resolveNode(nodeRef);

   if (node == null)
   {
      node = search.findNode(nodeRef);
      if (node === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
         return null;
      }
   }

   var permissionData = model.data,
      settable = node.getSettablePermissions(),
      canReadInherited = true;

   if (node.parent.hasPermission("ReadRecords"))
   {
      permissionData["inherited"] = parsePermissions(node.parent.getPermissions(), settable);
   }
   else
   {
      canReadInherited = false;
   }

   permissionData["canReadInherited"] = canReadInherited;

   model.data = permissionData;
}

getRmPermissions();