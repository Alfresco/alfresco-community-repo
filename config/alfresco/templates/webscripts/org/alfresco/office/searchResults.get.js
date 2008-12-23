var searchString = args.search;
var searchType = args.type;
var queryString;

if ((searchString != null) && (searchString != ""))
{
   searchString = searchString.replace(/\"/g, '\\"');

   if (searchType == "tag")
   {
      queryString = "PATH:\"/cm:categoryRoot/cm:taggable/cm:" + searchString + "/member\"";
   }
   else
   {
      queryString = "(TEXT:\"" + searchString + "\") OR (@cm\\:name:*" + searchString + "*)";
   }
}
else
{
   searchString = "";
   queryString = "";
}

var luceneResults = search.luceneSearch(queryString), results = new Array(), i, ii;
for (i = 0, ii = luceneResults.length; i < ii; i++)
{
   if (luceneResults[i].properties["cm:modified"] != null)
   {
      results.push(luceneResults[i]);
   }
}

model.results = results;