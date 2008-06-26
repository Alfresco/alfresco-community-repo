function getFilterQuery(filter, obj)
{
   var filterQuery = null;
   
   switch (String(filter))
   {
      case "TODO: recentlyModified":
         break;
         
      case "TODO: recentlyAdded":
         break;
      
      case "editingMe":
         filterQuery = "+PATH:\"" + obj.containerNode.qnamePath + "//*\" ";
         filterQuery += " +@cm\\:workingCopyOwner:" + person.properties.userName;
         break;
      
      case "TODO: editingOthers":
         break;
      
      default:
         filterQuery = "+PATH:\"" + obj.pathNode.qnamePath + "/*\" ";
         filterQuery += "-ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         break;
   }
   
   return filterQuery;
}
