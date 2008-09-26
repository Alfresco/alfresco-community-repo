<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blog.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function updateBlog(node)
{
   var arr = getBlogPropertiesArray();
	
   // check whether we already have the aspect added to the node.
   if (node.hasAspect(BLOG_DETAILS_ASPECT))
   {
      for (propName in arr)
      {
         node.properties[propName] = arr[propName];
      }
   }
   else
   {
      // if not, add the aspect on the fly
      node.addAspect(BLOG_DETAILS_ASPECT, arr);
   }
	
   node.save();
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
   return;
   }

   // update blog
   updateBlog(node);
	
   model.item = getBlogData(node);
}

main();
