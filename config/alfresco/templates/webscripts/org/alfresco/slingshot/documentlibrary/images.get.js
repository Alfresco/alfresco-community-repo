<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Main entry point: Create collection of images in the given space (and its subspaces)
 * @method main
 */
function main()
{
   var items = [],
      assets,
      filterParams,
      query;
   
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }
   
   // Use the "all" filter and an "images" type
   parsedArgs.type = "images";
   filterParams = Filters.getFilterParams("all", parsedArgs);
   
   // Sort the list before trimming to page chunks 
   assets = search.query(
   {
      query: filterParams.query,
      page:
      {
         maxItems: (filterParams.limitResults ? parseInt(filterParams.limitResults, 10) : 0)
      },
      sort: filterParams.sort
   });
   
   return (
   {
      luceneQuery: filterParams.query,
      items: assets
   });
}

/**
 * Images List Component: images
 */
model.images = main();