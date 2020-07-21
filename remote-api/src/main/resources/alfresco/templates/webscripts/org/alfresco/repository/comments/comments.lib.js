
/** Name used for the topic that contains all comments. */
const COMMENTS_TOPIC_NAME = "Comments";

/**
 * Returns all comment nodes for a given node.
 * @return an array of comments.
 */
function getComments(node)
{
   var commentsFolder = getCommentsFolder(node);
   if (commentsFolder !== null)
   {
      var elems = commentsFolder.childAssocs["cm:contains"];
      if (elems !== null)
      {
         return elems;
      }
   }
   // no comments found, return an empty array
   return [];
}

/**
 * Returns the folder that contains all the comments.
 * 
 * We currently use the fm:discussable aspect where we
 * add a "Comments" topic to it.
 */
function getCommentsFolder(node)
{
   if (node.hasAspect("fm:discussable"))
   {
      var forumFolder = node.childAssocs["fm:discussion"][0];
      var topicFolder = forumFolder.childByNamePath(COMMENTS_TOPIC_NAME);
      return topicFolder;
   }
   else
   {
      return null;
   }
}

/**
 * Creates the comments folder if it doesn't yet exist for the given node.
 */
function getOrCreateCommentsFolder(node)
{
   var commentsFolder = getCommentsFolder(node);
   if (commentsFolder == null)
   {    
      commentsFolder = commentService.createCommentsFolder(node); 
   }
   return commentsFolder;
}

/**
 * Returns the data object for a comment node
 */
function getCommentData(node)
{
   var data = {};
   data.node = node;
   data.author = people.getPerson(node.properties["cm:creator"]);
   data.isUpdated = (node.properties["cm:modified"] - node.properties["cm:created"]) > 5000;
   data.canEditComment = (person == data.owner) || (person == data.author) ||
                         node.hasPermission("SiteManager") || node.hasPermission("Coordinator");
   return data;
}

/**
 * Returns the count of comments for a node.
 */
function getCommentsCount(node)
{
   return getComments(node).length;
}
