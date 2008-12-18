<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

function ensureTagScope(node)
{
   if (! node.isTagScope)
   {
      node.isTagScope = true;
   }
   
   // also check the parent (the site!)
   if (! node.parent.isTagScope)
   {
      node.parent.isTagScope = true;
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
   model.externalBlogConfig = hasExternalBlogConfiguration(node);
   
   if (json.has("site") && json.has("container") && json.has("page") && !model.item.isDraft)
   {
      var data =
      {
         title: model.item.node.properties.title,
         page: json.get("page") + "?postId=" + model.item.node.properties.name
      };
      activities.postActivity("org.alfresco.blog.post-created", json.get("site"), "blog", jsonUtils.toJSONString(data));
   }
}

main();
