<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

/**
 * Deletes a blog post node.
 */
function deleteBlogPost(postNode)
{
   // delete the node
   var nodeRef = postNode.nodeRef;
   var isDeleted = postNode.remove();
   if (! isDeleted)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + nodeRef);
      return;
   }
   model.message = "Node " + nodeRef + " deleted";
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   var title = node.properties.title;
   var tags = node.properties.tags;
          
   deleteBlogPost(node);
   
   // post an activitiy item, but only if we got a site
   if (url.templateArgs.site != null)
   {
      var browsePostListUrl = '/page/site/' + url.templateArgs.site + '/blog-postlist?container=' + url.templateArgs.container;
      var data = {
          title: title,
          browsePostListUrl: browsePostListUrl
      }
      activities.postActivity("org.alfresco.blog.post-deleted", url.templateArgs.site, url.templateArgs.container, jsonUtils.toJSONString(data));
   }
}

main();
