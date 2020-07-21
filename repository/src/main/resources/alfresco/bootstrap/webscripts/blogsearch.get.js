// check that search term has been provided
if (args.q == undefined || args.q.length == 0)
{
   status.code = 400;
   status.message = "Search term has not been provided.";
   status.redirect = true;
}
else
{
   // perform search
   var nodes = search.luceneSearch("TEXT:" + args.q);
   model.resultset = nodes;
}
