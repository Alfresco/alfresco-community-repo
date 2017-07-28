<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

const POST_ACTION = "publish";
const UPDATE_ACTION = "update";
const REMOVE_ACTION = "unpublish";

/**
 * Validates the action to execute.
 * @return the action name to be used for the blog-post action or null if the specified action is invalid
 */
function validateAction(node, action)
{
   var blogAction = null;
   var isPublished = false;
   if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
   {
      isPublished = true;
   }

   // make sure we have a real JavaScript object, otherwise switch won't work correctly
   action = "" + action;
   switch (action)
   {
   case POST_ACTION:
      blogAction = (isPublished ? "" : "post");
      break;
   case UPDATE_ACTION:
      blogAction = (isPublished ? "update" : "");
      break;
   case REMOVE_ACTION:
      blogAction = (isPublished ? "remove" : "");
      break;
   }
   
   if (blogAction === null)
   {
      // set an error status
      status.setCode(status.STATUS_BAD_REQUEST, "Invalid action specified (node in wrong state?)");
      return null;
   }
   else
   {
      return blogAction;
   }
}

/**
 * Publishe, update or removes the blog from/to the external blog
 */
function executeAction(node, action)
{
   // get the blog action to call (the names differ from the constants defined above) 
   var blogAction = validateAction(node, action);
   if (blogAction != null)
   {
      var blog = actions.create("blog-post");
      blog.parameters.action = blogAction;
      blog.execute(node);
      
      // check whether we got an error - if result is non-empty
      if (blog.parameters["result"].length > 0)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, blog.parameters["result"]);
         return;
      }
   }
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   // fetch and execute the action
   var action = json.get("action");
   executeAction(node, action);

   // get the updated data for the blog post
   model.item = getBlogPostData(node);
   model.externalBlogConfig = hasExternalBlogConfiguration(node);
}

main();
