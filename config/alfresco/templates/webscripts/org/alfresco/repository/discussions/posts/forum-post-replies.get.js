<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/**
 * Fetches the nodes that are children of post
 */
function getRepliesForPost(post)
{
	var children = post.sourceAssocs["cm:references"];
	if (children === null)
	{
		return new Array();
	}
	else
	{
		return children;
	}
}

/**
 * Fetches the reply node and the child count.
 * Fetches the children in addition if levels > 1.
 */
function getReplyDataRecursive(post, levels)
{
	// encapsulates the data: node, childCount, children
	var data = new Object();
	data.post = post;
	var children = getRepliesForPost(post);
	data.childCount = children.length;
	if (levels > 1)
	{
		data.children = new Array();
		var x = 0;
		for (x =0; x < children.length; x++)
		{
			data.children.push(getReplyDataRecursive(children[x], levels-1));
		}
	}
	return data;
}

function getRepliesImpl(post, levels)
{
	var data = getReplyDataRecursive(post, levels + 1);
	if (data.children != undefined)
	{
		return data.children;
	}
	else
	{
		return new Array();
	}
}

function getReplies(node, levels)
{
	// we have to differentiate here whether this is a top-level post or a reply
	if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
	{
		// find the primary post node
		var data = getTopicPostData(node);  // PENDING: couldn't this be done in a more performant way?
		return getRepliesImpl(data.post, levels);
	}
	else if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
	{
		return getRepliesImpl(node, levels);
	}
	else
	{
		status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
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

	// process additional parameters
	var levels = args["levels"] != undefined ? parseInt(args["levels"]) : 1;

	model.data = getReplies(node, levels);
}

main();
