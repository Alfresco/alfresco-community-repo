var Filters =
{
   TYPE_MAP:
   {
      "documents": '+(TYPE:"{http://www.alfresco.org/model/content/1.0}content" OR TYPE:"{http://www.alfresco.org/model/application/1.0}filelink" OR TYPE:"{http://www.alfresco.org/model/content/1.0}folder")',
      "folders": '+TYPE:"{http://www.alfresco.org/model/content/1.0}folder"',
      "images": "-TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\" +@cm\\:content.mimetype:image/*"
   },

   getFilterParams: function Filter_getFilterParams(filter, parsedArgs, optional)
   {
      var filterParams =
      {
         query: "+PATH:\"" + parsedArgs.parentNode.qnamePath + "/*\"",
         limitResults: null,
         sortBy: "@{http://www.alfresco.org/model/content/1.0}name",
         sortByAscending: true,
         variablePath: false
      };

      optional = optional || {};

      // Max returned results specified?
      var argMax = args.max;
      if ((argMax !== null) && !isNaN(argMax))
      {
         filterParams.limitResults = argMax;
      }

      var favourites = optional.favourites;
      if (typeof favourites == "undefined")
      {
         favourites = [];
      }

      // Create query based on passed-in arguments
      var filterId = String(filter),
         filterData = String(args.filterData),
         filterQuery = "";

      // Common types and aspects to filter from the UI
      filterQueryDefaults = " -ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
      filterQueryDefaults += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"";
      filterQueryDefaults += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}systemfolder\"";
      filterQueryDefaults += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forums\"";
      filterQueryDefaults += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}forum\"";
      filterQueryDefaults += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\"";
      filterQueryDefaults += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"";

      switch (filterId)
      {
         case "all":
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
            filterParams.query = filterQuery + filterQueryDefaults;
            break;

         case "recentlyAdded":
         case "recentlyModified":
         case "recentlyCreatedByMe":
         case "recentlyModifiedByMe":
            var onlySelf = (filterData.indexOf("ByMe")) > 0 ? true : false,
               dateField = (filterData.indexOf("Created") > 0) ? "created" : "modified",
               ownerField = (dateField == "created") ? "creator" : "modifier";

            // Default to 7 days - can be overridden using "days" argument
            var dayCount = 7,
               argDays = args.days;
            if ((argDays !== null) && !isNaN(argDays))
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

            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath;
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
            filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";

            filterParams.sortBy = "@{http://www.alfresco.org/model/content/1.0}" + dateField;
            filterParams.sortByAscending = false;
            filterParams.variablePath = true;
            filterParams.query = filterQuery + filterQueryDefaults;
            break;

         case "editingMe":
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += " +ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
            filterQuery += " +@cm\\:workingCopyOwner:" + person.properties.userName;

            filterParams.variablePath = true;
            filterParams.query = filterQuery;
            break;

         case "editingOthers":
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += " +ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
            filterQuery += " -@cm\\:workingCopyOwner:" + person.properties.userName;

            filterParams.variablePath = true;
            filterParams.query = filterQuery;
            break;

         case "favouriteDocuments":
            var foundOne = false;

            for (var favourite in favourites)
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

         case "node":
            filterParams.query = "+ID:\"" + parsedArgs.rootNode.nodeRef + "\"";
            break;

         case "tag":
            filterParams.variablePath = true;
            filterParams.query = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\" +PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(filterData) + "/member\"";
            break;

         default:
            filterQuery = "+PATH:\"" + parsedArgs.parentNode.qnamePath + "/*\"";
            filterParams.query = filterQuery + filterQueryDefaults;
            break;
      }

      // Specialise by passed-in type
      filterParams.query += " " + (Filters.TYPE_MAP[parsedArgs.type] || "");

      return filterParams;
   }
};
