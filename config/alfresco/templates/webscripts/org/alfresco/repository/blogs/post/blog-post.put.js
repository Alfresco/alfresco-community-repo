<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Updates the draft mode part of the post
 */
function updateBlogPostDraftMode(postNode)
{
   // make sure the user doesn't try to put a non-draft
   // post back into draft node
   var currentDraft = (! postNode.hasAspect("blg:releaseDetails")) ||
                      (postNode.properties["blg:released"] == undefined);
   var isDraft = json.has("draft") && json.get("draft").toString() == "true";
   
   // requested draft, previously non-draft: throw an exception
   if (isDraft && ! currentDraft)
   {
       // set an error
      status.setCode(status.STATUS_BAD_REQUEST, "Cannot put a released post back into draft mode!");
      return null;
   }
   
   if (! isDraft)
   {
      setOrUpdateReleasedAndUpdatedDates(postNode);
   }
}

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
   
   // update the node
   postNode.properties.title = title;
   postNode.mimetype = "text/html";
   postNode.content = content
   postNode.tags = tags;
   postNode.save();
   
   updateBlogPostDraftMode(postNode);
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
   
   // post an activitiy item, but only if we got a site
   if (url.templateArgs.site != null && ! model.item.isDraft)
   {
      var browsePostUrl = '/page/site/' + url.templateArgs.site + '/blog-topicview?container=' + url.templateArgs.container + '&postId=' + node.name;      
      var data = {
          title: node.properties.title,
          browsePostUrl: browsePostUrl
      }
      activities.postActivity("org.alfresco.blog.post-updated", url.templateArgs.site, url.templateArgs.container, jsonUtils.toJSONString(data));
   }
}

main();
