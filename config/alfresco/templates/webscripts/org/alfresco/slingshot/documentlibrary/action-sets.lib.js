function getActionSet(asset, obj)
{
   var actionSet = "empty",
      assetType = obj.assetType,
      isLink = obj.isLink,
      itemStatus = obj.itemStatus,
      isItemOwner = (obj.itemOwner && obj.itemOwner.properties.userName == person.properties.userName);
   
   if (isLink)
   {
      actionSet = "link";
   }
   else if (asset.isContainer)
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
