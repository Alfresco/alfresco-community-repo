<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Creates a blog post
 */
function createBlogPost(blogNode)
{
   // fetch the data required to create the post
   var title = null;
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = null;
   if (json.has("content"))
   {
      content = json.get("content");
   }
   var tags = null;
   if (json.has("tags"))
   {
      // get the tags JSONArray and copy it into a real javascript array object
      var tmp = json.get("tags");
      tags = new Array();
      for (var x=0; x < tmp.length(); x++)
      {
          tags.push(tmp.get(x));
      }
   }
   
   // get a unique name
   var nodeName = getUniqueChildName(blogNode, "post");
   
   // we simply create a new file inside the blog folder
   var postNode = blogNode.createNode(nodeName, "cm:content");

   // set values where supplied
   if (title !== null)
   {
      postNode.properties.title = title;
   }   
   if (content !== null)
   {
      postNode.mimetype = "text/html";
      postNode.content = content;
   }
   /*if (tags !== null)
   {
      postNode.tags = tags;
   }*/
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
