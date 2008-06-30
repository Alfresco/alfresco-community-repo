<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function createReplyPost(topicNode, parentPostNode)
{
	// fetch the data required to create a topic
	var title = "";
	if (json.has("title"))
	{
		title = json.get("title");
	}
	var content = json.get("content");
	logger.log("Creating new post with title " + title + " and text " + content);

	// create the topic node, and add the first child node representing the topic text
	// NOTE: this is a change from the old web client, where the topic title was used as name for the node
	var name = getUniqueChildName(topicNode, "post");

	var postNode = topicNode.createNode(name, "fm:post");
	postNode.mimetype = "text/html";
	postNode.properties.title = title;
	postNode.content = content;
	postNode.save();
   
	// link it to the parent post
	postNode.addAspect("cm:referencing");
	postNode.createAssociation(parentPostNode, "cm:references");
	postNode.save(); // probably not necessary here

	return postNode;
}

function createPostReply(node)
{
	// we have to differentiate here whether this is a top-level post or a reply
	if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
	{
		// find the primary post node
		var data = getTopicPostData(node);
		var topic = data.topic;
		var post = data.post;
		return createReplyPost(topic, post);
	}
	else if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
	{
		// the forum is the paren of the node
		var topic = node.parent;
		var post = node;
		return createReplyPost(topic, post);
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
	
	model.post = createPostReply(node);
}

main();
