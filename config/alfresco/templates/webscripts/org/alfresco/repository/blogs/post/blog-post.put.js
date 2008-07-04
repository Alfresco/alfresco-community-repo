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
   var isDraft = json.get("draft") == "true";
   
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
   var content = json.get("content");
	
   // update the node
   postNode.properties.title = title;
   postNode.mimetype = "text/html";
   postNode.content = content;
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
}

main();
