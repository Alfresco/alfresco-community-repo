<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">

function getContents(user, type, maxResults)
{
   var padZeros = function padZeros(number)
   {
      return (number < 10) ? '0' + number : number;
   }
   
   //set range to within last 28 days
   var date = new Date();
   var toQuery = date.getFullYear() + "-" + padZeros((date.getMonth()+1)) + "-" + padZeros(date.getDate());
   date.setDate(date.getDate() - 28);
   var fromQuery = date.getFullYear() + "-" + padZeros((date.getMonth()+1)) + "-" + padZeros(date.getDate());
   
   var userProperty = (type == 'created') ? 'creator' : 'modifier';
   var query = 'PATH:"/*/st:sites/*/*//*" ' +
               'AND +@cm:' + userProperty + ':"' + user + '" ' +
               'AND +@cm:' + type + ':["' + fromQuery + '" TO "' + toQuery + '"] ' +
               'AND +TYPE:"cm:content" ' +
               'AND -TYPE:"ia:calendarEvent" ' +
               'AND -TYPE:"dl:dataListItem" ';
   
   // perform fts-alfresco language query
   var sortColumns = [];
   sortColumns.push(
   {
      column: "@" + utils.longQName("cm:" + type),
      ascending: false
   });
   var queryDef = {
      query: query,
      language: "fts-alfresco",
      page: {maxItems: maxResults},
      onerror: "no-results",
      sort: sortColumns
   };
   var nodes = search.query(queryDef);
   
   // reset processed results (in search.lib.js)
   processedCache = {}
   return processResults(nodes, maxResults);
}

var maxResults = (args.maxResults !== undefined) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS;

model.data = [];
model.data['created'] = getContents(args.user, 'created', maxResults);
model.data['modified'] = getContents(args.user, 'modified', maxResults);