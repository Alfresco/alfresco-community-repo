<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

/**
 * Deletes a topic node.
 */
function deleteTopicPost(topicNode)
{
	// we simply delete the topic
	var qnamePath = topicNode.qnamePath;
	logger.log("Deleting node " + qnamePath);
	var isDeleted = topicNode.remove();
	logger.log("Node deleted: " + isDeleted);
	if (! isDeleted)
	{
		status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + qnamePath);
		return;
	}
	model.message = "Node " + qnamePath + " deleted";
}

function addRepliesOfPostRecursive(node, arr)
{
	var replies = node.sourceAssocs["cm:references"];
	if (replies != null)
	{
		var x;
		for (x = 0; x < replies.length; x++)
		{
			addRepliesOfPostRecursive(replies[x], arr);
			arr.push(replies[x]);
		}
	}
}

/**
 * Deletes a reply post.
 */
function deleteReplyPost(postNode)
{
	// we have to delete the node as well as all replies
	// PENDING: what happens if the user has the right to delete its node
	// but not the replies to it?
	var nodes = new Array();
	addRepliesOfPostRecursive(postNode, nodes);
	nodes.push(postNode);
	
	var qnamePath = postNode.qnamePath
	var isDeleted = false;
	for (x = 0; x < nodes.length; x++)
	{
		isDeleted = nodes[x].remove();
		if (! isDeleted)
		{
			status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + nodes[x].nodeRef);
			return;
		}
	}
	model.message = "Node " + qnamePath + " deleted";
}
 
/**
 * Deletes a post.
 * Note: this function also deletes all replies of the post
 */
function deletePost(node)
{
	// simple case: topic post
	if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
	{
		deleteTopicPost(node);
	}
	else if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
	{
		deleteReplyPost(node);
	}
	else
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Node is not of type fm:topic or fm:post");
	}
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
