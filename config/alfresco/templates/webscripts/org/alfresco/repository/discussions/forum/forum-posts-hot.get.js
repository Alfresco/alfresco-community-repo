<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/forum/forum-posts.lib.js">

/**
 * Prepends a number with zeros to make it a 4numgth string
 */
function toFourDigitString(num)
{
	if (num < 10) return "000" + num;
	if (num < 100) return "00" + num;
	if (num < 1000) return "0" + num;
	return "" + num;
}

/**
 * Fetches the hot topics found in the forum.
 * Hot topics are the one with the most replies recently.
 *
 * We implement this follows: We fetch all posts in the forum, ordered by inverse
 * creation date and fetch the nodes for the first 20 posts
 */
function getHotTopicPostList(node, index, count)
{
	var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
					  " +PATH:\"" + node.qnamePath + "/*/*\"" +
					  " +ASPECT:\"cm:referencing\"";
	var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";
	posts = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, false);

	/** Implement sort and order logic to show the node with most replies first. */
	// PENDING: quick and dirty version, works but needs cleanup!
	var max = 20; // only process so many posts
	if (posts.length < max) max = posts.length;
	
	var idToNode = new Array();
	var idToCount = new Array();

	for (var x=0; x < max; x++)
	{
		var parent = posts[x].parent;
		var id = parent.nodeRef.id;
		if (idToCount[id] != null) {
			idToCount[id] = idToCount[id] + 1;
		} else {
			idToNode[id] = parent;
			idToCount[id] = 1;
		}
	}
	
	// get the list sorted by number of replies
	var tmp = new Array();
	for (var id in idToCount) {
		tmp.push(toFourDigitString(idToCount[id]) + id);
	}
	tmp.sort();
	tmp.reverse();
	
	var nodes = Array();
	for (var x=0; x < tmp.length; x++)
	{
		nodes.push(idToNode[tmp[x].substring(4)]);
	}

	// get the data
	return getTopicPostListData(nodes, index, count);
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
	model.data = getHotTopicPostList(node, index, count);
	
	model.contentFormat = (args["contentFormat"] != undefined) ? args["contentFormat"] : "full";
}

main();
