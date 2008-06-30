function getActionSet(asset, obj)
{
   var actionSet = "empty";
   var itemStatus = obj.itemStatus.toString();
   var isItemOwner = (obj.itemOwner == person.properties.userName);
   
   // Only 1 action set for folders
   if (asset.isContainer)
   {
      actionSet = "folder";
   }
   else if (itemStatus.indexOf("workingCopy") != -1)
   {
      actionSet = isItemOwner ? "workingCopyOwner" : "locked";
   }
   else if (itemStatus.indexOf("locked") != -1)
   {
      actionSet = isItemOwner ? "lockOwner" : "locked";
   }
   else
   {
      actionSet = "document";
   }
   
   return actionSet;
}
