<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function addComment(node)
{
    // fetch the data required to create a comment
    //var title = json.get("title");
    var content = json.get("content");
    logger.log("Creating new comment with text " + content);

	var commentsFolder = getOrCreateCommentsFolder(node);

	// get a unique name
    var name = getUniqueChildName(commentsFolder, "comment");
	
	// we simply create a new file inside the blog folder
    var commentNode = commentsFolder.createNode(name, "fm:post");
	commentNode.mimetype = "text/html";
    //commentNode.properties.title = title;
    commentNode.content = content;
    commentNode.save();
	 
    return commentNode;
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	var comment = addComment(node);
	model.item = comment;
}

main();
