// check category exists?
var category = search.luceneSearch("PATH:\"/cm:generalclassifiable//cm:" + url.extension + "\"");
if (category == undefined)
{
   status.code = 404;
   status.message = "Category " + url.extension + " not found.";
   status.redirect = true;
}
else
{
   // perform category search
   var nodes = search.luceneSearch("PATH:\"/cm:generalclassifiable//cm:" + url.extension + "//member\"");
   model.resultset = nodes;
}