<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Updates a blog post node
 */
function updateBlogPost(postNode)
{
   // fetch the new data
   var title = "";
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = json.get("content");
	
   // update the node
   postNode.properties.title = title;
   postNode.mimetype = "text/html";
   postNode.content = content;
   postNode.save();
	
   // PENDING:
   // check whether it is draft mode
   /*if (postNode.hasAspect("cm:workingcopy") && json.get("draft") == "false")
   {
      postNode.removeAspect("cm:workingcopy");
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

	// update blog post
	updateBlogPost(node);
	
	model.item = getBlogPostData(node);
}

main();
