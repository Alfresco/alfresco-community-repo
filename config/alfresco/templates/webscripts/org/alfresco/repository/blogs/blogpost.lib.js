<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Returns the data of a blog post.
 */
function getBlogPostData(node)
{
   var data = {};
   data.node = node;
   data.commentCount = getCommentsCount(node);
   
   // draft
   data.isDraft = node.hasAspect("cm:workingcopy");
   
   // isUpdated
   data.isUpdated = (node.properties["cm:modified"] - node.properties["cm:created"]) > 5000;
   
   // outOfDate
   if ((node.properties["blg:lastUpdate"] != undefined))
   {
      if ((node.properties["cm:modified"] - node.properties["blg:lastUpdate"]) > 5000)
      {
         data.outOfDate = true;
      }
      else
      {
         data.outOfDate = false;
      }
   }
   else
   {
      data.outOfDate = false;
   }
   
   return data;
}
