<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/**
 * Returns the correct post node to update-
 * This function makes sure that a post is returned in case the passed node is a topic.
 */
function findPostNode(node)
{
	if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
	{
		return node;
	}
	else if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
	{
		var nodes = getOrderedPosts(node);
		if (nodes.length > 0)
		{
			return nodes[0];
		}
		else
		{
			status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "First post of topic node" + node.nodeRef + " missing");
		}
	}
	else
	{
		status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
	}
}

/**
 * Creates a post inside the passed forum node.
 */
function updatePost(postNode)
{
	var title = "";
	if (json.has("title"))
	{
		title = json.get("title");
	}
	var content = json.get("content");
	
	// update the topic title
	postNode.properties.title = title;
	postNode.mimetype = "text/html";
	postNode.content = content;
	postNode.save();
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}
	
	// find the post node - returns the passed node in case node is a post,
	// or the primary post in case node is a topic
	/* Due to https://issues.alfresco.com/browse/ALFCOM-1775
	   we will do the search here as we have to reuse the lucene result later
	var postNode = findPostNode(node);
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}*/
	var postNode = null;
	if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
	{
		postNode = node;
	}
	else if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
	{
		var nodes = getOrderedPosts(node);
		if (nodes.length > 0)
		{
			postNode = nodes[0];
		}
		else
		{
			status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "First post of topic node" + node.nodeRef + " missing");
			return;
		}
	}
	else
	{
		status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
		return;
	}
	
	// update
	updatePost(postNode);
	
	// fetch the data to render the result
	if (node.nodeRef.equals(postNode.nodeRef)) // false for topic posts
	{
		model.post = node;
	}
	else
	{
		//model.topicpost = getTopicPostData(node);
		model.topicpost = getTopicPostDataFromTopicAndPosts(node, nodes);
	}
}

main();
