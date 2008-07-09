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
   
   // update the node
   if (title !== null)
   {
      postNode.properties.title = title;
   }
   if (content !== null)
   {
      postNode.mimetype = "text/html";
      postNode.content = content
   }
   /*if (tags !== null)
   {
      postNode.tags = tags;
   }*/
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
