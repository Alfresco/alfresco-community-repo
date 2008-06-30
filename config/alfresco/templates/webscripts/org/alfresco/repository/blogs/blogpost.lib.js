
const DRAFT_FOLDER_NAME = "Drafts";

/**
 * Returns the draft folder.
 */
function getOrCreateDraftsFolder(node)
{
    var draftFolder = node.childByNamePath(DRAFT_FOLDER_NAME);
	if (draftFolder == null)
	{
	    draftFolder = node.createNode(DRAFT_FOLDER_NAME, "cm:folder");
	}
	return draftFolder;
}

function getCommentsCount(node)
{
   // check whether there are comments
   // PENDING: this should use the comments API
   if (node.hasAspect("fm:discussable"))
   {
       var forumNode = node.childAssocs["fm:discussion"][0];
	   var topicNode = forumNode.childAssocs["cm:contains"][0];
       return topicNode.childAssocs["cm:contains"].length
   }
   else
   {
      return 0;   
   }
}

/**
 * Returns the data of a blog post.
 */
function getBlogPostData(node)
{
   var data = {};
   data.node = node;
   data.commentCount = getCommentsCount(node);
   
   // draft
   data.isDraft = node.hasAspect("cm:workingcopy");
   
   // isUpdated
   data.isUpdated = (node.properties["cm:modified"] - node.properties["cm:created"]) > 5000;
   
   // outOfDate
   if ((node.properties["blg:lastUpdate"] != undefined))
   {
		if ((node.properties["cm:modified"] - node.properties["blg:lastUpdate"]) > 5000)
		{
			data.outOfDate = true;
		}
		else
		{
			data.outOfDate = false;
		}
   }
   else
   {
	  data.outOfDate = false;
   }
   
   return data;
}

/**
 * Returns the data of a blog post.
 */
/*function getBlogPostData(node)
{
   return node;
}*/

/**
 * Returns an array containing all topics found in the passed array.
 * Filters out non-fm:topic nodes.
 */
function getBlogPostListData(nodes, index, count)
{
   var items = new Array();
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++) 
   {
      items.push(getBlogPostData(nodes[i]));
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
function getBlogPostListByLuceneQuery(node, luceneQuery, sortAttribute, ascending, index, count)
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
   return getBlogPostListData(nodes, index, count);
}

