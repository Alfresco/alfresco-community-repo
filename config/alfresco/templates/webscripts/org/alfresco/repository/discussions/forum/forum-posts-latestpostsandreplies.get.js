<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/forum/forum-posts.lib.js">


function getLatestAddedPostsListData(nodes, index, count)
{
   var items = new Array();
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++) 
   {
      // fetch the topic post data and then add the node as reply.
	  // in case of a new topicpost, the reply and the post will be the
	  // same, otherwise they differ. The topic post will be used to fetch
	  // the topic title, while the reply will be used to show the text
	  var data = getTopicPostData(nodes[i].parent);
	  data.reply = nodes[i];
      items.push(data);
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
 * Fetches all posts in reverse order.
 */
function getLatestAddedPosts(node, index, count)
{
	var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
					  " +PATH:\"" + node.qnamePath + "/*/*\"";
	var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";
	nodes = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, false);

	// get the data
	return getLatestAddedPostsListData(nodes, index, count);
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	// process additional parameters
	var index = args["startIndex"] != undefined ? parseInt(args["startIndex"]) : 0;
	var count = args["pageSize"] != undefined ? parseInt(args["pageSize"]) : 10;
	
	// fetch the data and assign it to the model
	model.data = getLatestAddedPosts(node, index, count);
	
	model.contentFormat = (args["contentFormat"] != undefined) ? args["contentFormat"] : "full";
}

main();
var x = 0;
