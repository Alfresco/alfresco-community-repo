<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function createPost(forumNode)
{
   // fetch the data required to create a topic
   var title = json.get("title");
   var content = json.get("content");
   logger.log("Creating New post " + title + " with text " + content);
   
   // create the topic node, and add the first child node representing the topic text
   // NOTE: this is a change from the old web client, where the topic title was used as name for the node
   var name = getUniqueChildName(forumNode, "post");
   var topicNode = forumNode.createNode(name, "fm:topic");

   // We use twice the same name for the topic and the post in it
   var contentNode = topicNode.createNode(name, "fm:post");
   contentNode.mimetype = "text/html";
   contentNode.properties.title = title;
   contentNode.content = content;
   contentNode.save();

   return topicNode;
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	var topicPost = createPost(node);
	model.topicpost = getTopicPostData(topicPost);
}

main();
