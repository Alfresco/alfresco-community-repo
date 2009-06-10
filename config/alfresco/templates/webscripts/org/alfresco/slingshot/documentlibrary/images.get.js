<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Images List Component: images
 */
model.images = getImageList();

/* Create collection of images in the given space (and its subspaces) */
function getImageList()
{
   var items = new Array(), assets, filterParams, query;
   
   // Use helper function to get the arguments
   var parsedArgs = getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }
   
   // Use the "all" filter
   filterParams = getFilterParams("all", parsedArgs);
   query = filterParams.query;
   // Specialise by image type
   query += " " + getTypeFilterQuery("images");
   
   // Sort the list before trimming to page chunks 
   assets = search.luceneSearch(query, filterParams.sortBy, filterParams.sortByAscending, filterParams.limitResults ? filterParams.limitResults : 0);
   
   return (
   {
      luceneQuery: filterParams.query,
      items: assets
   });
}