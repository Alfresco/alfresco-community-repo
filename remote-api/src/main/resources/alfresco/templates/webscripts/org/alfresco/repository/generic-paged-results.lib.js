/**
 * Returns a data object that can be passed to the paged results template
 * for rendering.
 *
 * @param nodes: The complete result set nodes
 * @param index: the start index from which results should be returned
 * @param count: the number of elements that should be returned
 * @param extractDataFn: The function that extracts the data to be returned
 *                       for each node in the final data set.
 *                       The functions signature is name(index, node).
 * 
 * Returns an array containing all topics found in the passed array.
 * Filters out non-fm:topic nodes.
 */
function getPagedResultsData(nodes, index, count, extractDataFn)
{
   var items = new Array();
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++) 
   {
      items.push(extractDataFn(nodes[i]));
      added++;
   }
   
   return (
   {
      "total" : nodes.length,
      "pageSize" : count,
      "startIndex" : index,
      "itemCount" : items.length,
      "items": items
   });
}

/**
 * Returns a list of topics, as returned by the lucene query
 */
function getPagedResultsDataByLuceneQuery(node, luceneQuery, sortAttribute, ascending, index, count, extractDataFn)
{
   var nodes = null;
   if (sortAttribute != null)
   {
      nodes = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, ascending);
   }
   else
   {
      nodes = search.luceneSearch(luceneQuery);
   }
   return getPagedResultsData(nodes, index, count, extractDataFn);
}

