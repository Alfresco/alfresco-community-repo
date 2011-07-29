<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/searchutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/links/links.lib.js">

const DEFAULT_NUM_DAYS = 7;

/**
 * Fetches all links added to the site
 */
function getLinksList(node, filter, tag, numdays, index, count)
{
   // query information
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/linksmodel/1.0}link\" +PATH:\"" + node.qnamePath + "/*\"";
   
   if (filter == "recent")
   {
      var fromDate = getTodayMinusXDays(DEFAULT_NUM_DAYS);
      var toDate = new Date();

      luceneQuery += getCreationDateRangeQuery(fromDate, toDate);
   }
   else if (filter == "user")
   {
      luceneQuery += " +@cm\\:creator:\"" + person.properties.userName + '"';
   }
   
   if (tag !== null)
   {
      luceneQuery += " +PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(tag) + "/member\" ";
   }
   
   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";
   
   // get the data
   return getPagedResultsDataByLuceneQuery(node, luceneQuery, sortAttribute, false, index, count, getLinksData);
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }
   model.links = node;
   
   // Grab the paging and filtering details
   var pNumber = parseInt(args.page, 10);
   var pSize = parseInt(args.pageSize, 10);
   var filter = args.filter;
   var tag = (args.tag != undefined && args.tag.length > 0) ? args.tag : null;
   
   // Paging is required, ensure the parameters were given
   if ((pNumber === undefined) || (pSize === undefined) ||
       isNaN(pNumber) || isNaN(pSize))
   {
      var message = "Page sizing parameters missing!";
      status.code = 400; // Bad Request
      status.message = message;
      model.message = message;
      return;
   }

   model.data = getLinksList(node,filter,tag,7,(pNumber - 1) * pSize,pSize);
}

main();
