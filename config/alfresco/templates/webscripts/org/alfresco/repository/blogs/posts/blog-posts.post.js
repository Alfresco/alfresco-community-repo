<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

function ensureTagScope(node)
{
   if (! node.isTagScope)
   {
      node.isTagScope = true;
   }
}

/**
 * Creates a blog post
 */
function createBlogPost(blogNode)
{
   // fetch the data required to create the post
   var title = "";
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = "";
   if (json.has("content"))
   {
      content = json.get("content");
   }
   var tags = [];
   if (json.has("tags"))
   {
      // get the tags JSONArray and copy it into a real javascript array object
      var tmp = json.get("tags");
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
   postNode.properties.title = title;
   postNode.mimetype = "text/html";
   postNode.content = content;
   postNode.tags = tags;
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
	
	ensureTagScope(node);

	var post = createBlogPost(node);
	model.item = getBlogPostData(post);
	
   // post an activitiy item, but only if we got a site
   if (url.templateArgs.site != null && ! model.item.isDraft)
   {
      var browsePostUrl = '/page/site/' + url.templateArgs.site + '/blog-postview?container=' + url.templateArgs.container + '&postId=' + post.name;      
      var data = {
          title: post.properties.title,
          browsePostUrl: browsePostUrl
      }
      activities.postActivity("org.alfresco.blog.post-created", url.templateArgs.site, url.templateArgs.container, jsonUtils.toJSONString(data));
   }
}

main();
