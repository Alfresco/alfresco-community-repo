function getFilterParams(filter, parsedArgs)
{
   var filterParams =
   {
      query: "+PATH:\"" + parsedArgs.parentNode.qnamePath + "/*\"",
      limitResults: null,
      sortBy: "@{http://www.alfresco.org/model/content/1.0}name",
      sortByAscending: true,
      variablePath: false
   }

   // Max returned results specified?
   var argMax = args["max"];
   if ((argMax != null) && !isNaN(argMax))
   {
      filterParams.limitResults = argMax;
   }

   // Create query based on passed-in arguments
   switch (String(filter))
   {
      case "all":
         var filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}systemfolder\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forums\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forum\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"";
         filterParams.query = filterQuery;
         break;
         
      case "node":
         filterParams.variablePath = true;
         filterParams.query = "+ID:\"" + parsedArgs.parentNode.nodeRef + "\"";
         break;
      
      case "tag":
         filterParams.variablePath = true;
         filterParams.query = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\" +PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(args["filterData"]) + "/member\"";
         break;
      
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

         // Default limit to 50 documents - can be overridden using "max" argument
         if (filterParams.limitResults === null)
         {
            filterParams.limitResults = 50;
         }
         
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();
         date.setDate(date.getDate() - dayCount);
         var fromQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();

         var filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
         filterQuery += " +@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00.000 TO " + toQuery + "T23\\:59\\:59.999]";
         filterQuery += " -ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}systemfolder\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forums\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forum\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"";

         filterParams.sortBy = "@{http://www.alfresco.org/model/content/1.0}" + dateField;
         filterParams.sortByAscending = false;
         filterParams.variablePath = true;
         filterParams.query = filterQuery;
         break;
         
      case "editingMe":
         var filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
         filterQuery += " +ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         filterQuery += " +@cm\\:workingCopyOwner:" + person.properties.userName;

         filterParams.variablePath = true;
         filterParams.query = filterQuery;
         break;
      
      case "editingOthers":
         var filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
         filterQuery += " +ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         filterQuery += " -@cm\\:workingCopyOwner:" + person.properties.userName;

         filterParams.variablePath = true;
         filterParams.query = filterQuery;
         break;
      
      default:
         var filterQuery = "+PATH:\"" + parsedArgs.parentNode.qnamePath + "/*\"";
         filterQuery += " -ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}systemfolder\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forums\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forum\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"";
         
         filterParams.query = filterQuery;
         break;
   }
   
   return filterParams;
}

const TYPE_MAP =
{
   "documents": '+(TYPE:"{http://www.alfresco.org/model/content/1.0}content" OR TYPE:"{http://www.alfresco.org/model/application/1.0}filelink" OR TYPE:"{http://www.alfresco.org/model/content/1.0}folder")',
   "folders": '+TYPE:"{http://www.alfresco.org/model/content/1.0}folder"',
   "images": "-TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\" +@cm\\:content.mimetype:image/*"
};

function getTypeFilterQuery(type)
{
   return TYPE_MAP[type] || "";
}
