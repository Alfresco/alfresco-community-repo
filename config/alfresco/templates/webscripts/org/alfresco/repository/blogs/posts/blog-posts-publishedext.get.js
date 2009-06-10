<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/searchutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Fetches all posts of the given blog
 */
function getBlogPostList(node, index, count)
{
   // query information
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
                       " +PATH:\"" + node.qnamePath + "/*\" ";
   luceneQuery += " +(@\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:application/octet-stream OR";
   luceneQuery += "  @\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:text/html)"

   // add the drafts part
   luceneQuery += "+ASPECT:\"{http://www.alfresco.org/model/blogintegration/1.0}blogPost\" "

   var sortAttribute = "@{http://www.alfresco.org/model/blogintegration/1.0}posted";

   // get the data
   return getPagedResultsDataByLuceneQuery(node, luceneQuery, sortAttribute, false, index, count, getBlogPostData);
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
   
   // fetch and assign the data
   model.data = getBlogPostList(node, index, count);

   // fetch the contentLength param
   var contentLength = args["contentLength"] != undefined ? parseInt(args["contentLength"]) : -1;
   model.contentLength = isNaN(contentLength) ? -1 : contentLength;
   
   // assign the blog node
   model.blog = node;
   model.externalBlogConfig = hasExternalBlogConfiguration(node);
}

main();
