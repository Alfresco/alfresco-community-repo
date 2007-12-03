var searchString = args.search;
var searchType = args.type;
var queryString;

if ((searchString != null) && (searchString != ""))
{
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

model.results = search.luceneSearch(queryString);

