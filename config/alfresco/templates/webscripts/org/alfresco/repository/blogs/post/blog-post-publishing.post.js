<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

var POST_ACTION = "publish";
var UPDATE_ACTION = "update";
var REMOVE_ACTION = "unpublish";

function executeAction(node, action)
{
   var blogAction = "";
   var isPublished = false;
   if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
   {
      isPublished = true;
   }

   
   // make sure we have a real JavaScript object,
   // otherwise switch won't work correctly
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
 
   if (blogAction != "")
   {
      var blog = actions.create("blog-post");
      blog.parameters.action = blogAction;
      blog.execute(node);
      result = blog.parameters["result"];
	  logger.log("Blog action result: " + result);
	  model.message = result;
	  /* Check whether action succeeded
	  if (result != "sfsdf")
	  {
		
      }*/
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
	model.item = getBlogPostData(node);
}

main();
