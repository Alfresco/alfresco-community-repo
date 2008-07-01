<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

function getLatestAddedPostData(node)
{
   // fetch the topic post data and then add the node as reply.
   // in case of a new topic post, the reply and the post will be the
   // same, otherwise they differ. The topic post will be used to fetch
   // the topic title, while the reply will be used to show the text
   var data = getTopicPostData(node.parent);
   data.reply = node;
   data.isRootPost = data.post.nodeRef.equals(data.reply.nodeRef);
   return data;
}

/**
 * Fetches all posts in reverse order.
 */
function getLatestAddedPosts(node, index, count)
{
	var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
					  " +PATH:\"" + node.qnamePath + "/*/*\"";
	var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";

	// get the data
	return getPagedResultsDataByLuceneQuery(node, luceneQuery, sortAttribute, false, index, count, getLatestAddedPostData);
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
