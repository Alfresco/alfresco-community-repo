var searchString = args.search,
   searchType = args.type,
   maxResults = args.maxresults,
   queryString,
   results = [];

if (maxResults == null)
{
   maxResults = 10;
}

if ((searchString != null) && (searchString != ""))
{
   // searchString = searchString.replace(/\"/g, '\\"');
   searchString = search.ISO9075Encode(searchString);

   if (searchType == "tag")
   {
      queryString = "PATH:\"/cm:categoryRoot/cm:taggable/cm:" + searchString + "/member\"";
   }
   else
   {
      queryString = "(TEXT:\"" + searchString + "\") OR (@cm\\:name:*" + searchString + "*)";
   }
   
   queryString = "+(" + queryString + ") +(ISNOTNULL:\"cm:modified\")"

   results = search.luceneSearch(queryString, "@{http://www.alfresco.org/model/content/1.0}name", true, maxResults);
}
else
{
   searchString = "";
   queryString = "";
}

model.results = results;