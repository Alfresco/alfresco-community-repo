<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">
function main()
{
   var params =
   {
      siteId: args.site,
      containerId: args.container,
      repo: (args.repo !== null) ? (args.repo == "true") : false,
      term: args.term,
      tag: args.tag,
      query: args.query,
      rootNode: args.rootNode,
      sort: args.sort,
      maxResults: (args.maxResults !== null) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS,
      pageSize: (args.pageSize !== null) ? parseInt(args.pageSize, 10) : DEFAULT_PAGE_SIZE,
      startIndex: (args.startIndex !== null) ? parseInt(args.startIndex, 10) : 0,
      facetFields: args.facetFields,
      filters: args.filters,
      spell: (args.spellcheck !== null) ? (args.spellcheck == "true") : false
   };
   
   model.data = getSearchResults(params);
}

main();