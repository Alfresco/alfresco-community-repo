<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Deletes a topic node.
 */
function deleteComment(node)
{
	// we simply delete the topic
	var qnamePath = node.qnamePath;
	logger.log("Deleting node " + qnamePath);
	var isDeleted = node.remove();
	logger.log("Node deleted: " + isDeleted);
	if (! isDeleted)
	{
		status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + qnamePath);
		return;
	}
	
	// also remove the discussable aspect if there are no more comments
	deleteCommentsFolder(node);
	
	model.message = "Node " + qnamePath + " deleted";
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	deleteComment(node);
}

main();
