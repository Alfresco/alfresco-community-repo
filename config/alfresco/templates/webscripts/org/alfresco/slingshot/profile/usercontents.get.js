<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">

function getContents(user, type, maxResults)
{
   var padZeros = function padZeros(number)
   {
      return (number<10) ? '0'+number : number;
   }
   //set range to within last 28 days
   var date = new Date();
   var toQuery = date.getFullYear() + "\\-" + padZeros((date.getMonth()+1)) + "\\-" + padZeros(date.getDate());
   date.setDate(date.getDate() - 28);
   var fromQuery = date.getFullYear() + "\\-" + padZeros((date.getMonth()+1)) + "\\-" + padZeros(date.getDate());

   var userType = (type=='created') ? 'creator' : 'modifier';
   
   var query = "+PATH:\"/app:company_home/st:sites/*//*\" "+
               "+TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" " +
               "+@cm\\:" + userType + ":\"" + user + "\" " +
               "+@cm\\:" + type + ":[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]" +
               "-TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"";
   
   var nodes = search.luceneSearch(query, "cm:"+type, false, maxResults);
   //reset processed results (in search.lib.js)
   processedCache = {}
   return processResults(nodes, maxResults);
}

var maxResults = (args.maxResults !== undefined) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS;

model.data = [];
model.data['created'] = getContents(args.user, 'created', maxResults);
model.data['modified'] = getContents(args.user, 'modified', maxResults);