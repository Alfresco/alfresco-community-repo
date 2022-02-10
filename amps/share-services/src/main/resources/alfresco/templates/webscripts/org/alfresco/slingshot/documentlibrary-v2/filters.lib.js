var Filters =
{
   /**
    * Type map to filter required types.
    * NOTE: "documents" filter also returns folders to show UI hint about hidden folders.
    */
   TYPE_MAP:
   {
      "documents": '+(TYPE:"content" OR TYPE:"app:filelink" OR TYPE:"folder")',
      "folders": '+(TYPE:"folder" OR TYPE:"app:folderlink")',
      "images": '+@cm\\:content.mimetype:image/*'
   },
   
   /**
    * Types that we want to suppress from the resultset
    */
   IGNORED_TYPES:
   [
      "cm:systemfolder",
      "fm:forums",
      "fm:forum",
      "fm:topic",
      "fm:post"
   ],
   
   /**
    * Aspects ignored from the canned query based resultset
    */
   IGNORED_ASPECTS:
   [
      "cm:checkedOut"
   ],

   /**
    * Encode a path with ISO9075 encoding
    *
    * @method iso9075EncodePath
    * @param path {string} Path to be encoded
    * @return {string} Encoded path
    */
   iso9075EncodePath: function Filter_iso9075EncodePath(path)
   {
      var parts = path.split("/");
      for (var i = 1, ii = parts.length; i < ii; i++)
      {
         parts[i] = "cm:" + search.ISO9075Encode(parts[i]);
      }
      return parts.join("/");
   },

   /**
    * Create filter parameters based on input parameters
    *
    * @method getFilterParams
    * @param filter {string} Required filter
    * @param parsedArgs {object} Parsed arguments object literal
    * @param optional {object} Optional arguments depending on filter type
    * @return {object} Object literal containing parameters to be used in Lucene search
    */
   getFilterParams: function Filter_getFilterParams(filter, parsedArgs, optional)
   {
      var filterParams =
      {
         query: "+PATH:\"" + parsedArgs.pathNode.qnamePath + "/*\"",
         limitResults: null,
         sort: [
         {
            column: "@cm:name",
            ascending: true
         }],
         language: "lucene",
         templates: null,
         variablePath: true,
         ignoreTypes: Filters.IGNORED_TYPES,
         ignoreAspects: Filters.IGNORED_ASPECTS
      };

      optional = optional || {};

      // Sorting parameters specified?
      var sortAscending = args.sortAsc,
         sortField = args.sortField;

      if (sortAscending == "false")
      {
         filterParams.sort[0].ascending = false;
      }
      if (sortField !== null)
      {
         filterParams.sort[0].column = (sortField.indexOf(":") != -1 ? "@" : "") + sortField;
      }

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
      var filterData = String(args.filterData),
         filterQuery = "";

      // Common types and aspects to filter from the UI - known subtypes of cm:content and cm:folder
      var filterQueryDefaults = ' -TYPE:"' + Filters.IGNORED_TYPES.join('" -TYPE:"') + '"';

      switch (String(filter))
      {
         case "all":
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += " +TYPE:\"cm:content\"";
            filterQuery += " -ASPECT:\"cm:checkedOut\"";
            filterParams.query = filterQuery + filterQueryDefaults;
            break;

         case "recentlyAdded":
         case "recentlyModified":
         case "recentlyCreatedByMe":
         case "recentlyModifiedByMe":
            var onlySelf = (filter.indexOf("ByMe")) > 0 ? true : false,
               dateField = (filter.indexOf("Modified") > 0) ? "modified" : "created",
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

            filterQuery = this.constructPathQuery(parsedArgs);
            filterQuery += " +@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00.000 TO " + toQuery + "T23\\:59\\:59.999]";
            if (onlySelf)
            {
               filterQuery += " +@cm\\:" + ownerField + ":\"" + person.properties.userName + '"';
            }
            filterQuery += " +TYPE:\"cm:content\"";

            filterParams.sort = [
            {
               column: "@cm:" + dateField,
               ascending: false
            }];
            filterParams.query = filterQuery + filterQueryDefaults;
            break;

         case "editingMe":
            filterQuery = this.constructPathQuery(parsedArgs);
            filterQuery += " +((+@cm\\:workingCopyOwner:\"" + person.properties.userName + '")';
            filterQuery += " OR (+@cm\\:lockOwner:\"" + person.properties.userName + '"';
            filterQuery += " +@cm\\:lockType:\"WRITE_LOCK\"))";
            filterParams.query = filterQuery;
            break;

         case "editingOthers":
            filterQuery = this.constructPathQuery(parsedArgs);
            filterQuery += " +((+ASPECT:\"workingcopy\"";
            filterQuery += " -@cm\\:workingCopyOwner:\"" + person.properties.userName + '")';
            filterQuery += " OR (-@cm\\:lockOwner:\"" + person.properties.userName + '"';
            filterQuery += " +@cm\\:lockType:\"WRITE_LOCK\"))";
            filterParams.query = filterQuery;
            break;

         case "favourites":
            for (var favourite in favourites)
            {
               if (filterQuery)
               {
                  filterQuery += " OR ";
               }
               filterQuery += "ID:\"" + favourite + "\"";
            }
            
            if (filterQuery.length !== 0)
            {
               filterQuery = "+(" + filterQuery + ")";
               // no need to specify path here for all sites - IDs are exact matches
               if (parsedArgs.nodeRef != "alfresco://sites/home" && parsedArgs.nodeRef != "alfresco://company/home")
               {
                  filterQuery += ' +PATH:"' + parsedArgs.rootNode.qnamePath + '//*"';
               }
            }
            else
            {
               // empty favourites query
               filterQuery = "+ID:\"\"";
            }
            
            filterParams.query = filterQuery;
            break;

         case "synced":
            filterQuery = this.constructPathQuery(parsedArgs);
            filterQuery += " +ASPECT:\"sync:syncSetMemberNode\"";
            filterParams.query = filterQuery;
            break;

         case "syncedErrors":
            filterQuery = this.constructPathQuery(parsedArgs);
            filterQuery += " +ASPECT:\"sync:failed\"";
            filterParams.query = filterQuery;
            break;

         case "node":
            filterParams.variablePath = false;
            filterParams.query = "+ID:\"" + parsedArgs.nodeRef + "\"";
            break;

         case "tag":
            // Remove any trailing "/" character
            if (filterData.charAt(filterData.length - 1) == "/")
            {
               filterData = filterData.slice(0, -1);
            }
            filterQuery = this.constructPathQuery(parsedArgs);
            filterParams.query = filterQuery + " +PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(filterData) + "/member\"";
            break;

         case "category":
            // Remove any trailing "/" character
            if (filterData.charAt(filterData.length - 1) == "/")
            {
               filterData = filterData.slice(0, -1);
            }
            filterQuery = this.constructPathQuery(parsedArgs);
            filterParams.query = filterQuery + " +PATH:\"/cm:generalclassifiable" + Filters.iso9075EncodePath(filterData) + "/member\"";
            break;

         case "aspect":
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += "+ASPECT:\"" + args.filterData + "\"";
            filterParams.query = filterQuery;
            break;

         default: // "path"
            filterParams.variablePath = false;
            filterQuery = "+PATH:\"" + parsedArgs.pathNode.qnamePath + "/*\"";
            filterParams.query = filterQuery + filterQueryDefaults;
            break;
      }

      // Specialise by passed-in type
      if (filterParams.query !== "")
      {
         filterParams.query += " " + (Filters.TYPE_MAP[parsedArgs.type] || "");
      }

      return filterParams;
   },
   
   constructPathQuery: function constructPathQuery(parsedArgs)
   {
      var pathQuery = "";
      if (parsedArgs.libraryRoot != companyhome || parsedArgs.nodeRef != "alfresco://company/home")
      {
         if (parsedArgs.nodeRef == "alfresco://sites/home")
         {
            // all sites query - better with //cm:*
            pathQuery = '+PATH:"' + parsedArgs.rootNode.qnamePath + '//cm:*"';
         }
         else
         {
            // site specific query - better with //*
            pathQuery = '+PATH:"' + parsedArgs.rootNode.qnamePath + '//*"';
         }
      }
      return pathQuery;
   }
};
