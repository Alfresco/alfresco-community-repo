<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

/**
 * Deletes a topic node.
 */
function deletePost(postNode)
{
	// we simply delete the topic
	var qnamePath = postNode.qnamePath;
	logger.log("Deleting node " + qnamePath);
	var isDeleted = postNode.remove();
	logger.log("Node deleted: " + isDeleted);
	if (! isDeleted)
	{
		status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + qnamePath);
		return;
	}
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

	deletePost(node);
}

main();
