function getFilterParams(filter, parsedArgs, favourites)
{
   var filterParams =
   {
      query: "+PATH:\"" + parsedArgs.parentNode.qnamePath + "/*\"",
      limitResults: null,
      sortBy: "@{http://www.alfresco.org/model/content/1.0}name",
      sortByAscending: true,
      variablePath: false
   };

   // Max returned results specified?
   var argMax = args["max"];
   if ((argMax != null) && !isNaN(argMax))
   {
      filterParams.limitResults = argMax;
   }
   
   if (typeof favourites == "undefined")
   {
      favourites = [];
   }

   // Create query based on passed-in arguments
   var strFilter = String(filter);
   switch (strFilter)
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
      
      case "recentlyAdded":
      case "recentlyModified":
      case "recentlyCreatedByMe":
      case "recentlyModifiedByMe":
         var onlySelf = (strFilter.indexOf("ByMe")) > 0 ? true : false,
            dateField = (strFilter.indexOf("Created") > 0) ? "created" : "modified",
            ownerField = (dateField == "created") ? "creator" : "modifier";
         
         // Default to 7 days - can be overridden using "days" argument
         var dayCount = 7,
            argDays = args["days"];
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

         var filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath;
         if (parsedArgs.nodeRef == "alfresco://sites/home")
         {
            // Special case for "Sites home" pseudo-nodeRef
            filterQuery += "/*/cm:documentLibrary";
         }
         filterQuery += "//*\"";
         filterQuery += " +@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00.000 TO " + toQuery + "T23\\:59\\:59.999]";
         if (onlySelf)
         {
            filterQuery += " +@cm\\:" + ownerField + ":" + person.properties.userName;
         }
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
      
      case "favouriteDocuments":
         var filterQuery = "",
            foundOne = false;
         
         for (favourite in favourites)
         {
            if (foundOne)
            {
               filterQuery += " OR ";
            }
            foundOne = true;
            filterQuery += "ID:\"" + favourite + "\"";
         }
         filterParams.variablePath = true;
         filterParams.query = filterQuery.length > 0 ? "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\" +(" + filterQuery + ")" : "+ID:\"\"";
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
