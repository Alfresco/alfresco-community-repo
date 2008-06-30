function getFilterQuery(filter, obj)
{
   var filterQuery = null;
   
   switch (String(filter))
   {
      case "recentlyModified":
         var usingModified = true;
         // fall through...
      case "recentlyAdded":
         // Which query: created, or modified?
         var dateField = "modified";
         if (typeof usingModified === "undefined")
         {
            dateField = "created";
         }
         
         // Default to 7 days - can be overridden using "days" argument
         var dayCount = 7;
         var argDays = args["days"];
         if ((argDays != null) && !isNaN(argDays))
         {
            dayCount = argDays;
         }
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();
         date.setDate(date.getDate() - dayCount);
         var fromQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();

         filterQuery = "+PATH:\"" + obj.containerNode.qnamePath + "//*\" ";
         filterQuery += "+@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59] ";
         filterQuery += "-ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         break;
         
      case "editingMe":
         filterQuery = "+PATH:\"" + obj.containerNode.qnamePath + "//*\" ";
         filterQuery += "+ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\" ";
         filterQuery += "+@cm\\:workingCopyOwner:" + person.properties.userName;
         break;
      
      case "editingOthers":
         filterQuery = "+PATH:\"" + obj.containerNode.qnamePath + "//*\" ";
         filterQuery += "+ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\" ";
         filterQuery += "-@cm\\:workingCopyOwner:" + person.properties.userName;
         break;
      
      default:
         filterQuery = "+PATH:\"" + obj.pathNode.qnamePath + "/*\" ";
         filterQuery += "-ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         break;
   }
   
   return filterQuery;
}
