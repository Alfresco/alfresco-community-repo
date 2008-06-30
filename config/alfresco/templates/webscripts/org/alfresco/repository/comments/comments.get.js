<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">



/**
 * Fetches all posts found in the forum.
 */
function getCommentsList(node, index, count)
{
	var nodes = new Array();
	
    // comments are added through a custom child association ("cm:comments") that links to a cm:folder that holds all the comment files
	var commentFolder = getCommentsFolder(node);
	if (commentFolder != null)
	{
		nodes = commentFolder.childAssocs["cm:contains"];
	}

	return getCommentListData(nodes, index, count);
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

	model.data = getCommentsList(node, index, count);
}

main();
