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
      
      // Data for search term highlighting...
      highlightFields: (args.highlightFields !== null) ? args.highlightFields : DEFAULT_HIGHLIGHT_FIELDS,
      highlightPrefix: (args.highlightPrefix !== null) ? args.highlightPrefix : DEFAULT_HIGHLIGHT_POSTFIX,
      highlightPostfix: (args.highlightPostfix !== null) ? args.highlightPostfix : DEFAULT_HIGHLIGHT_POSTFIX,
      highlightSnippetCount: (args.highlightSnippetCount !== null) ? parseInt(args.highlightSnippetCount, 10) : DEFAULT_HIGHLIGHT_SNIPPET_COUNT,
      highlightFragmentSize: (args.highlightFragmentSize !== null) ? parseInt(args.highlightFragmentSize, 10) : DEFAULT_HIGHLIGHT_FRAGMENT_SIZE,
      highlightMaxAnalyzedChars: (args.highlightMaxAnalyzedChars !== null) ? parseInt(args.highlightMaxAnalyzedChars, 10) : DEFAULT_HIGHLIGHT_MAX_ANALYZED_CHARS,
      highlightUsePhraseHighlighter: (args.highlightUsePhraseHighlighter !== null) ? args.highlightUsePhraseHighlighter.toUpperCase() === "TRUE" : DEFAULT_HIGHLIGHT_USE_PHRASE_HIGHLIGHTER,
      highlightMergeContiguous: (args.highlightMergeContiguous !== null) ? args.highlightMergeContiguous.toUpperCase() === "TRUE" : DEFAULT_HIGHLIGHT_MERGE_CONTIGUOUS,
      
      encodedFilters: args.encodedFilters,
      spell: (args.spellcheck !== null) ? (args.spellcheck == "true") : false
   };
   
   model.data = getSearchResults(params);
}

main();
