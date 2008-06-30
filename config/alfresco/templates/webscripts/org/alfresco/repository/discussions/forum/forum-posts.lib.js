<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js"> 

/**
 * Returns an array containing all topics found in the passed array.
 * Filters out non-fm:topic nodes.
 */
function getTopicPostListData(nodes, index, count)
{
   var items = new Array();
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++) 
   {
      items.push(getTopicPostData(nodes[i]));
	  added++;
   }
   
   return ({
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
function getTopicPostListByLuceneQuery(node, luceneQuery, sortAttribute, ascending, index, count)
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
   return getTopicPostListData(nodes, index, count);
}
