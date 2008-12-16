<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/searchutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/links/links.lib.js">


/**
 * Fetches all links added to the site
 */
function getLinksList(node,filter,tag,numdays, index, count)
{
   //var fromDate = getTodayMinusXDays(numdays);

   // query information
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/linksmodel/1.0}link\"" +
                     " +PATH:\"" + node.qnamePath + "/*\"";
    
   if (filter == "internal")
   {
      //luceneQuery += " +@cm\\:internal:\"true\"";
       luceneQuery += "+ASPECT:\"{http://www.alfresco.org/model/linksmodel/1.0}internal\" ";
   }
   else if (filter == "www")
   {
      luceneQuery += "-ASPECT:\"{http://www.alfresco.org/model/linksmodel/1.0}internal\" ";
   }

   if (tag != null)
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
    
   var pNumber = parseInt(args.page);
   var pSize = parseInt(args.pageSize);
   var filter = args.filter;
   var tag = (args["tag"] != undefined && args["tag"].length > 0) ? args["tag"] : null;
    
   if ((pNumber == undefined) || (pSize == undefined))
   {
      model.error = "Parameters missing!";
      return;
   }
    model.data = getLinksList(node,filter,tag,7,(pNumber - 1) * pSize,pSize);
  
    
}

main();