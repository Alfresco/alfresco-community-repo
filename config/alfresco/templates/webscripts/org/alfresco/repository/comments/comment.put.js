<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function updateComment(node)
{
	/*var title = "";
	if (json.has("title"))
	{
		title = json.get("title");
	}*/
	var content = json.get("content");
	
	// update the topic title
	//postNode.properties.title = title;
	node.mimetype = "text/html";
	node.content = content;
	node.save();
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	// update
	updateComment(node);
	
	model.item = node;
}

main();
