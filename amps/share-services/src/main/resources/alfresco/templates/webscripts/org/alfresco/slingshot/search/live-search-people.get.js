<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/live-search.lib.js">

function main()
{
   if (args.t === null || args.t.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Query terms must be provided");
      return;
   }
   
   var params =
   {
      type: "people",
      term: args.t,
      maxResults: (args.maxResults !== null) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS,
      startIndex: (args.startIndex !== null) ? parseInt(args.startIndex, 10) : 0
   };
   
   model.data = liveSearch(params);
}

main();