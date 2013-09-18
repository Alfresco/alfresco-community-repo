<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">

var maxResults = (args.maxResults !== undefined) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS;

function padZeros(number)
{
   return (number < 10) ? '0' + number : number;
}

function getContents(user, type)
{
   // set range to within last 28 days
   var date = new Date();
   var toQuery = date.getFullYear() + "-" + padZeros((date.getMonth()+1)) + "-" + padZeros(date.getDate());
   date.setDate(date.getDate() - 28);
   var fromQuery = date.getFullYear() + "-" + padZeros((date.getMonth()+1)) + "-" + padZeros(date.getDate());
   
   var userProperty = (type == 'created') ? 'creator' : 'modifier';
   
   var getBlogPostsQuery = function getBlogPosts()
   {
      return 'PATH:"/app:company_home/st:sites/*/cm:blog/*" ' +
             'AND +TYPE:"cm:content" ' +
             'AND +@cm:' + userProperty + ':"' + user + '" ' +
             'AND +@cm:' + type + ':["' + fromQuery + '" TO "' + toQuery + '"]';
   };
   
   var getWikiPagesQuery = function getWikiPagesQuery()
   {
      return 'PATH:"/app:company_home/st:sites/*/cm:wiki/*" ' +
             'AND +TYPE:"cm:content" ' +
             'AND +@cm:' + userProperty + ':"' + user + '" ' +
             'AND +@cm:' + type + ':["' + fromQuery + '" TO "' + toQuery + '"]';
   };
   
   var getDiscussionsQuery = function getDiscussionsQuery()
   {
      return 'PATH:"/app:company_home/st:sites/*/cm:discussions//*" ' +
             'AND +TYPE:"fm:post" ' +
             'AND +@cm:' + userProperty + ':"' + user + '" ' +
             'AND +@cm:' + type + ':["' + fromQuery + '" TO "' + toQuery + '"]';
   };
   
   var getDocumentsQuery = function getDocumentsQuery()
   {
      return 'TYPE:"cm:content" ' +
             'AND +@cm:' + userProperty + ':"' + user + '" ' +
             'AND +@cm:' + type + ':["' + fromQuery + '" TO "' + toQuery + '"]';
   };
   
   var sortColumns = [];
   sortColumns.push(
   {
      column: "@" + utils.longQName("cm:" + type),
      ascending: false
   });
   
   var queryDef = {
      query: "",
      language: "fts-alfresco",
      page: {maxItems: maxResults},
      onerror: "no-results",
      sort: sortColumns
   };
   
   // perform fts-alfresco language queries
   var results;
   queryDef.query = getBlogPostsQuery();
   results = search.query(queryDef);
   queryDef.query = getWikiPagesQuery();
   results = results.concat(search.query(queryDef));
   queryDef.query = getDiscussionsQuery();
   results = results.concat(search.query(queryDef));
   queryDef.query = getDocumentsQuery();
   results = results.concat(search.query(queryDef));
   
   results.sort(function(a, b)
      {
         var date1 = a.properties[type].getTime(),
             date2 = b.properties[type].getTime();
         return (date1 < date2) ? 1 : (date1 > date2) ? -1 : 0;
      }
   );
   
   return processResults(results, maxResults);
}

model.data = [];
model.data['created'] = getContents(args.user, 'created', maxResults);
model.data['modified'] = getContents(args.user, 'modified', maxResults);