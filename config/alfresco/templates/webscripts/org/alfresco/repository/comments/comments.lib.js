
/**
 * Returns the folder that contains all the comments.
 * We currently use the forum model for testing purpose
 * PENDING: use a proper model!
 */ 
function getCommentsFolder(node)
{
	if (node.hasAspect("fm:discussable"))
	{
		var forumFolder = node.childAssocs["fm:discussion"][0];
		// we simply take the first topic folder in it
		// PENDING: this is error prone!
		var topicFolder = forumFolder.childAssocs["cm:contains"][0];
		return topicFolder;
	}
	else
	{
		return null;
	}
}

/**
 * Creates the comments folder if it doesn't yet exist for the given node.
 */
function getOrCreateCommentsFolder(node)
{
	var commentsFolder = getCommentsFolder(node);
	if (commentsFolder != null)
	{
		return commentsFolder;
	}
	
	node.addAspect("fm:discussable");
	var forumNode = node.createNode("Comments Forum", "fm:forum", "fm:discussion");
	commentsFolder = forumNode.createNode("Comments", "fm:topic", "cm:contains");
	return commentsFolder;
}

/**
 * Deletes the comments folder for a node if there are no comments in it.
 */
function deleteCommentsFolder(node)
{
	var commentsFolder = getCommentFolder(node);
	if (commentsFolder != null && commentsFolder.childAssocs["cm:contains"] == 0)
	{
		var forumFolder = node.childAssocs["fm:discussion"][0];
		node.removeNode(forumNode);
		node.removeAspect("fm:discussable");
	}
}

function getCommentData(node)
{
	return node;
}

/**
 * Returns an array containing all topics found in the passed array.
 * Filters out non-fm:topic nodes.
 */
function getCommentListData(nodes, index, count)
{
   var items = new Array();
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++) 
   {
      items.push(getCommentData(nodes[i]));
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
/*function getBlogsListByLuceneQuery(node, luceneQuery, sortAttribute, ascending, index, count)
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
   return getBlogListData(nodes, index, count);
}*/
