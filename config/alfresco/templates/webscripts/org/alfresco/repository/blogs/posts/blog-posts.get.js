<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/searchutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Fetches all posts of the given blog
 */
function getBlogPostList(node, fromDate, toDate, tag, index, count)
{
   // query information
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
                     " +PATH:\"" + node.qnamePath + "/*\"";
                       
   // include all published + my drafts
   luceneQuery += " ((" +
                    " -ASPECT:\"{http://www.alfresco.org/model/blogintegration/1.0}releaseDetails\" " +
                    "+@cm\\:creator:\"" + person.properties.userName + "\"" +
                   ") OR (" +
                    " +ASPECT:\"{http://www.alfresco.org/model/blogintegration/1.0}releaseDetails\" " +
                   ")) ";
                       
   // date query ?
   if (fromDate != null || toDate != null)
   {
      luceneQuery += getCreationDateRangeQuery(fromDate, toDate);
   }
   
   // is a tag selected?
   if (tag != null)
   {
      luceneQuery += " +PATH:\"/cm:taggable/cm:" + tag /*ISO9075.encode(tag)*/ + "/member\" ";
   }

   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";

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

   // begin and end date
   var fromDate = null;
   if (args["fromDate"] != undefined)
   {
      var tmp = parseInt(args["fromDate"]);
      if (tmp != Number.NaN)
      {
         fromDate = new Date(tmp);
      }
   }
   var toDate = null;
   if (args["toDate"] != undefined)
   {
      var tmp = parseInt(args["toDate"]);
      if (tmp != Number.NaN)
      {
         toDate = new Date(tmp);
      }
   }
   
   // selected tag
   var tag = args["tag"] != undefined && args["tag"].length > 0 ? args["tag"] : null;
   
   // fetch and assign the data
   model.data = getBlogPostList(node, fromDate, toDate, tag, index, count);
   model.contentFormat = (args["contentFormat"] != undefined) ? args["contentFormat"] : "full";
}

main();
