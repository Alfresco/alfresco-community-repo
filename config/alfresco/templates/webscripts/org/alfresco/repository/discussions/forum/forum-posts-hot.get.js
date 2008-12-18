<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

var MAX_NUM_OF_PROCESSED_POSTS = 20;

/**
 * Fetches the hot topics found in the forum.
 * Hot topics are topics with the most replies over the last x days.
 *
 * The current implementation fetches all posts in the forum ordered by inverse
 * creation date. It then analyzes the last x posts and fetches the topics thereof,
 * keeping track of the number of posts for each.
 * 
 * Note: We only look at topics with replies, the others will therefore not show up
 *       in that list.
 */
function getHotTopicPostList(node, index, count)
{
   // get the posts to check
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
                 " +PATH:\"" + node.qnamePath + "/*/*\"" +
                 " +ASPECT:\"cm:referencing\"";
   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}published";
   var posts = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, false);

   // check how many posts we check in the result set 
   var max = MAX_NUM_OF_PROCESSED_POSTS;
   if (posts.length < max)
   {
      max = posts.length;
   }
   
   // get for each the topic, keeping track of the number of replies and the first occurance
   // of the post.
   var idToData = {};
   for (var x = 0; x < max; x++)
   {
      // get the topic node (which is the direct parent of the post)
      var parent = posts[x].parent;
      var id = parent.nodeRef.id;
      if (idToData[id] != null)
      {
         idToData[id].count += 1;
      }
      else
      {
         idToData[id] =
         {
            count: 1,
            pos: x,
            node: parent
         };
      }
   }
   
   // copy the elements to an array as we will have to sort it
   // afterwards
   var dataArr = new Array();
   for (n in idToData)
   {
      dataArr.push(idToData[n]);
   }
   
   // sort the elements by number of replies, then by the position
   var sorter = function(a, b)
   {
       if (a.count != b.count)
       {
           // more replies first
           return b.count - a.count
       }
       else
       {
           // lower pos first
           return a.pos - b.pos;
       }
   }
   dataArr.sort(sorter);

   // extract now the nodes
   var nodes = Array();
   for (var x = 0; x < dataArr.length; x++)
   {
      nodes.push(dataArr[x].node);
   }

   // get the paginated data
   return getPagedResultsData(nodes, index, count, getTopicPostData);
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   // process additional parameters
   var index = args["startIndex"] != undefined ? parseInt(args["startIndex"]) : 0;
   var count = args["pageSize"] != undefined ? parseInt(args["pageSize"]) : 10;
   
   // fetch the data and assign it to the model
   model.data = getHotTopicPostList(node, index, count);
   
   // fetch the contentLength param
   var contentLength = args["contentLength"] != undefined ? parseInt(args["contentLength"]) : -1;
   model.contentLength = isNaN(contentLength) ? -1 : contentLength;
   
   // also set the forum node
   model.forum = node;
}

main();
