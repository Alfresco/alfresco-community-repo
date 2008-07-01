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
   var name = getUniqueChildName(blogNode, "post");
   
   // we simply create a new file inside the blog folder
   var postNode = blogNode.createNode(name, "cm:content");
   postNode.mimetype = "text/html";
   postNode.properties.title = title;
   postNode.content = content;
   postNode.save();
   
   // check whether it is draft mode
   if (json.get("draft") == "true")
   {
      // let's for now add a marker aspect, we can still change that later
	  var props = new Array();
	  props["cm:workingCopyOwner"] = "admin"; // PENDING
	  postNode.addAspect("cm:workingcopy", props);
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
