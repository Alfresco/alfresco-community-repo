/**
 * This library contains functions to work with topics (aka top level posts).
 *
 * Top-level posts are different from post that act as replies in that
 * they have some additional data available:
 * lastReplyOn:
 * lastReplyFrom: 
 * totalReplyCount:
 */

 
 /** Returns the topic posts for a topic, ordered by creation date.
 * We use this for two things: To find the root node and the last reply
 */
function getOrderedPosts(topic)
{
   var query = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
               " +PATH:\"" + topic.qnamePath + "/*\" ";
   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";
   return search.luceneSearch(topic.nodeRef.storeRef.toString(), query, sortAttribute, true) ;
}

/*
 * This method encapsulates a topic object into a JavaScript object with the following properties
 * node: The topic node
 * content: The topic text
 * replyCount: The number of replies
 */
function getTopicPostData(topicNode)
{  
    // fetch the posts
    var posts = getOrderedPosts(topicNode);
   
	return getTopicPostDataFromTopicAndPosts(topicNode, posts);
}

function getTopicPostDataFromTopicAndPosts(topicNode, posts)
{
    // check the first post (which is the main post)
    if (posts.length < 1)
	{
		status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "First post of topic node" + topicNode.nodeRef + " missing");
		return;
    }
	
	var item = new Object();
	
	// fetch the data
	item.topic = topicNode;
	item.post = posts[0];
    item.totalReplyCount = posts.length - 1;
	// in case of replies, find the last reply
	if (posts.length > 1)
	{
		item.lastReply = posts[posts.length - 1];
	}

    return item;
}
