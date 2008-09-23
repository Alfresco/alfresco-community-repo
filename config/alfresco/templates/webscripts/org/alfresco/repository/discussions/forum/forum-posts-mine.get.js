<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/**
 * Fetches all posts added to the forum in the last numdays days
 */
function getTopicPostList(node, index, count)
{
   // query information
   var luceneQuery = "+TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\" " +
                     "+PATH:\"" + node.qnamePath + "/*\" " +
                     "+@cm\\:creator:\"" + person.properties.userName + "\"";
   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}published";
  
   // get the data
   return getPagedResultsDataByLuceneQuery(node, luceneQuery, sortAttribute, false, index, count, getTopicPostData);
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
   model.data = getTopicPostList(node, index, count);
   
   // fetch the contentLength param
   var contentLength = args["contentLength"] != undefined ? parseInt(args["contentLength"]) : -1;
   model.contentLength = isNaN(contentLength) ? -1 : contentLength;
   
   // also set the forum node
   model.forum = node;
}

main();
