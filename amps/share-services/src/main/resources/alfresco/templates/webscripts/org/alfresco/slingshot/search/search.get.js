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
      encodedFilters: args.encodedFilters,
      spell: (args.spellcheck !== null) ? (args.spellcheck == "true") : false
   };

   if (args.highlightFields)
   {
      // Data for search term highlighting...
      params.highlightFields = args.highlightFields;
      params.highlightPrefix = (args.highlightPrefix !== null) ? args.highlightPrefix : DEFAULT_HIGHLIGHT_POSTFIX;
      params.highlightPostfix = (args.highlightPostfix !== null) ? args.highlightPostfix : DEFAULT_HIGHLIGHT_POSTFIX;
      params.highlightSnippetCount = (args.highlightSnippetCount !== null) ? parseInt(args.highlightSnippetCount, 10) : DEFAULT_HIGHLIGHT_SNIPPET_COUNT;
      params.highlightFragmentSize = (args.highlightFragmentSize !== null) ? parseInt(args.highlightFragmentSize, 10) : DEFAULT_HIGHLIGHT_FRAGMENT_SIZE;
      params.highlightUsePhraseHighlighter = (args.highlightUsePhraseHighlighter !== null) ? args.highlightUsePhraseHighlighter.toUpperCase() === "TRUE" : DEFAULT_HIGHLIGHT_USE_PHRASE_HIGHLIGHTER;
      params.highlightMergeContiguous = (args.highlightMergeContiguous !== null) ? args.highlightMergeContiguous.toUpperCase() === "TRUE" : DEFAULT_HIGHLIGHT_MERGE_CONTIGUOUS;
      
      if (args.highlightMaxAnalyzedChars !== null)
      {
         params.highlightMaxAnalyzedChars = parseInt(args.highlightMaxAnalyzedChars, 10)
      }
   }
   
   model.data = getSearchResults(params);
}

main();
