/**
* Query templates for custom search
*/
Filters.QUERY_TEMPLATES =
[
   { field: "keywords", template: "%(cm:name cm:title cm:description TEXT)" },
   { field: "name", template: "%(cm:name)" },
   { field: "title", template: "%(cm:title)" },
   { field: "description", template: "%(cm:description)" },
   { field: "creator", template: "%(cm:creator)" },
   { field: "created", template: "%(cm:created)" },
   { field: "modifier", template: "%(cm:modifier)" },
   { field: "modified", template: "%(cm:modified)" },
   { field: "author", template: "%(cm:author)" },
   { field: "markings", template: "%(rmc:supplementalMarkingList)" },
   { field: "dispositionEvents", template: "%(rma:recordSearchDispositionEvents)" },
   { field: "dispositionActionName", template: "%(rma:recordSearchDispositionActionName)" },
   { field: "dispositionActionAsOf", template: "%(rma:recordSearchDispositionActionAsOf)" },
   { field: "dispositionEventsEligible", template: "%(rma:recordSearchDispositionEventsEligible)" },
   { field: "dispositionPeriod", template: "%(rma:recordSearchDispositionPeriod)" },
   { field: "hasDispositionSchedule", template: "%(rma:recordSearchHasDispositionSchedule)" },
   { field: "dispositionInstructions", template: "%(rma:recordSearchDispositionInstructions)" },
   { field: "dispositionAuthority", template: "%(rma:recordSearchDispositionAuthority)" },
   { field: "holdReason", template: "%(rma:recordSearchHoldReason)" },
   { field: "vitalRecordReviewPeriod", template: "%(rma:recordSearchVitalRecordReviewPeriod)" }
];

Filters.IGNORED_TYPES =
[
   /* Defaults */
   "cm:systemfolder",
   "fm:forums",
   "fm:forum",
   "fm:topic",
   "fm:post",
   /* Records Management */
   "rma:dispositionSchedule",
   "rma:dispositionActionDefinition",
   "rma:dispositionAction",
   "rma:hold",
   "rma:transfer"
];

/**
 * Create filter parameters based on input parameters
 *
 * @method getFilterParams
 * @param filter {string} Required filter
 * @param parsedArgs {object} Parsed arguments object literal
 * @param optional {object} Optional arguments depending on filter type
 * @return {object} Object literal containing parameters to be used in Lucene search
 */
Filters.getFilterParams = function RecordsManagementFilter_getFilterParams(filter, parsedArgs, optional)
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
      ignoreTypes: Filters.IGNORED_TYPES
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

   // Create query based on passed-in arguments
   var filterData = args.filterData,
      filterQuery = "";

   // Common types and aspects to filter from the UI
   var filterQueryDefaults = ' -TYPE:"' + Filters.IGNORED_TYPES.join('" -TYPE:"') + '"';

   // Create query based on passed-in arguments
   switch (String(filter))
   {
      case "all":
         filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
         filterQuery += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
         filterParams.query = filterQuery + filterQueryDefaults;
         break;

      case "node":
         parsedArgs.pathNode = parsedArgs.rootNode.parent;
         filterParams.variablePath = false;
         filterParams.query = "+ID:\"" + parsedArgs.rootNode.nodeRef + "\"";
         break;

      case "savedsearch":
         var searchNode = parsedArgs.location.siteNode.getContainer("Saved Searches");
         if (searchNode != null)
         {
            var ssNode = searchNode.childByNamePath(String(filterData));

            if (ssNode != null)
            {
               var ssJson = eval('try{(' + ssNode.content + ')}catch(e){}');
               filterQuery = ssJson.query;
               // Wrap the query so that only valid items within the filePlan are returned
               filterParams.query = 'PATH:"' + parsedArgs.rootNode.qnamePath + '//*" AND (' + filterQuery + ')';
               filterParams.templates = Filters.QUERY_TEMPLATES;
               filterParams.language = "fts-alfresco";
               filterParams.namespace = "http://www.alfresco.org/model/recordsmanagement/1.0";
               // gather up the sort by fields
               // they are encoded as "property/dir" i.e. "cm:name/asc"
               if (ssJson.sort && ssJson.sort.length !== 0)
               {
                  var sortPairs = ssJson.sort.split(",");
                  var sort = [];
                  for (var i=0, j; i<sortPairs.length; i++)
                  {
                     if (sortPairs[i].length !== 0)
                     {
                        j = sortPairs[i].indexOf("/");
                        sort.push(
                        {
                           column: sortPairs[i].substring(0, j),
                           ascending: (sortPairs[i].substring(j+1) == "asc")
                        });
                     }
                  }
                  filterParams.sort = sort;
               }
            }
         }
         break;

      case "transfers":
         if (filterData == null)
         {
            filterParams.variablePath = false;
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += " +TYPE:\"rma:transfer\"";
            filterParams.query = filterQuery;
            filterParams.ignoreTypes = "";
         }
         else
         {
            filterParams.query = "+PARENT:\"" + filterData + "\"";
         }
         break;

      case "holds":
         if (filterData == null)
         {
            filterParams.variablePath = false;
            filterQuery = "+PATH:\"" + parsedArgs.rootNode.qnamePath + "//*\"";
            filterQuery += " +TYPE:\"rma:hold\"";
            filterParams.query = filterQuery;
            filterParams.ignoreTypes = "";
         }
         else
         {
            filterParams.query = "+PARENT:\"" + filterData + "\"";
         }
         break;

      default:
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
};