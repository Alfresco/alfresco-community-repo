<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Returns the date representing the begin of a month (the first day at 00:00:00)
 */
function getBeginOfMonthDate(date)
{
   return new Date(date.getFullYear(), date.getMonth(), 1);
}

/**
 * Returns the date representing the last second of a month (23:59:59)
 */
function getEndOfMonthDate(date)
{
   var year = date.getFullYear();
   var month = date.getMonth();
   var beginOfNextMonth = new Date(year, month + 1, 1); // will increment year if month > 11 
   return new Date(beginOfNextMonth.getTime() - 1); // one less to get the last millisecond of the previous day
}

/**
 * Create an object containing information about the month specified by date.
 */
function getMonthDataObject(date)
{
   var data = {};
   data.year = date.getFullYear();
   data.month = date.getMonth();
   data.firstPostInMonth = date;
   data.beginOfMonth = getBeginOfMonthDate(date);
   data.endOfMonth = getEndOfMonthDate(date);
   data.count = 1;
   return data;
}

/**
 * Fetches data for each month for which posts exist, plus the count of each.
 * Note: If no posts could be found, this method will return the current month
 *       but with a count of posts equals zero.
 */
function getBlogPostMonths(node)
{
   // query information
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
                     " +PATH:\"" + node.qnamePath + "/*\" " +
                     " +ISNOTNULL:\"{http://www.alfresco.org/model/content/1.0}published\" ";
   luceneQuery += " +(@\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:application/octet-stream OR";
   luceneQuery += "  @\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:text/html)"


   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}published";
   nodes = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, true);
   
   // will hold the months information
   var data = new Array();
   
   // do we have posts?
   if (nodes.length > 0)
   {
      var currYear = -1;
      var currMonth = -1;
      var currData = null;
      for (var x=0; x < nodes.length; x++)
      {
         var date = nodes[x].properties["cm:published"];
         
         // is this a new month?
         if (currYear != date.getFullYear() || currMonth != date.getMonth())
         {
            currYear = date.getFullYear();
            currMonth = date.getMonth();
            currData = getMonthDataObject(date);
            data.push(currData);
         }
         // otherwise just increment the counter
         else
         {
            currData.count += 1;
         }
      }
   }
   // if not, add the current month with count = 0
   else
   {
      var emptyData = getMonthDataObject(new Date());
      emptyData.count = 0;
      data.push(emptyData);
   }
   
   return data;
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   // fetch the months
   model.data = getBlogPostMonths(node);
}

main();
