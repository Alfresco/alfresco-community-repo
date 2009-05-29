<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">

const MAX_RESULTS = 3;

function getContents(user,type,maxResults)
{
   //set range to within last 28 days
   var date = new Date();
   var toQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
   date.setDate(date.getDate() - 28);
   var fromQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
   
   var query = "+PATH:\"/app:company_home//*\" "+
               "+TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" " +
               "+@cm\\:modifier:" + user + " " +
               "+@cm\\:" + type + ":[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]";

   var nodes = [];
   nodes = search.luceneSearch(query);

   return processResults(nodes, maxResults);
}

model.data = [];
model.data['created'] = getContents(args.user,'created',MAX_RESULTS);
model.data['modified'] = getContents(args.user,'modified',MAX_RESULTS);
model.user = args.user;

