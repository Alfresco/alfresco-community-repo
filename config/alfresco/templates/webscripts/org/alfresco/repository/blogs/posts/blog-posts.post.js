<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Creates a blog post
 */
function createBlogPost(blogNode)
{
   // fetch the data required to create the post
   var title = json.get("title");
   var content = json.get("content");
   
   // get a unique name
   var nodeName = getUniqueChildName(blogNode, "post");
   
   // we simply create a new file inside the blog folder
   var postNode = blogNode.createNode(nodeName, "cm:content");
   postNode.mimetype = "text/html";
   postNode.properties.title = title;
   postNode.content = content;
   postNode.save();
   
   // check whether it is in draft mode
   var isDraft = json.has("draft") && json.get("draft").toString() == "true";
   if (isDraft)
   {
      // disable permission inheritance. The result is that only
      // the creator will have access to the draft
      postNode.setInheritsPermissions(false);
   }
   else
   {
      setOrUpdateReleasedAndUpdatedDates(postNode);
   }

   return postNode;
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	var post = createBlogPost(node);
	model.item = getBlogPostData(post);
}

main();
