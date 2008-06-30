<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function updatePost(postNode)
{
	/*var name = "";
	if (json.has("name"))
	{
		title = json.get("name");
	}*/
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
	
   // check whether it is draft mode
   /*if (postNode.hasAspect("cm:workingcopy") && json.get("draft") == "false")
   {
      postNode.removeAspect("cm:workingcopy");
   }*/
	
	// try to change the file name
	/*if (name.length > 0)
	{
		postNode.name = name;
	}*/
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
	updatePost(node);
	
	model.item = getBlogPostData(node);
}

main();
