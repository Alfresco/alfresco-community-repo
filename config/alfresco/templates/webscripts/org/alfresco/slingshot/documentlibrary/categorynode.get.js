/**
 * Document List Component: category node
 */
model.categorynode = getCategoryNode();

/* Create collection of categories for the given path */
function getCategoryNode()
{
   try
   {
      var items = new Array(),
         hasSubfolders = true,
         evalChildFolders = args["children"] !== "false";
   
      var catAspect = (args["aspect"] != null) ? args["aspect"] : "cm:generalclassifiable",
         nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id,
         path = url.templateArgs.path,
         rootCategories = classification.getRootCategories(catAspect),
         rootNode, parent, categoryResults;

      if (rootCategories != null && rootCategories.length > 0)
      {
         rootNode = rootCategories[0].parent;
         if (path == null)
         {
            categoryResults = classification.getRootCategories(catAspect);
         }
         else
         {
            var queryPath = "/" + catAspect + "/" + encodePath(path);
            categoryResults = search.luceneSearch("+PATH:\"" + queryPath + "/*\" -PATH:\"" + queryPath + "/member\"");
         }
         
         // make each result an object and indicate it is selectable in the UI
         for each (item in categoryResults)
         {
            if (evalChildFolders)
            {
               hasSubfolders = item.children.length > 0;
            }

            items.push(
            {
               node: item,
               hasSubfolders: hasSubfolders
            });
         }
      }
   
      items.sort(sortByName);
   
      return (
      {
         items: items
      });
   }
   catch(e)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
      return;
   }
}

/* Get the path as an ISO9075 encoded path */
function encodePath(path)
{
   var parts = path.split("/");
   for (var i = 0, ii = parts.length; i < ii; i++)
   {
      parts[i] = "cm:" + search.ISO9075Encode(parts[i]);
   }
   return parts.join("/");
}

/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.node.name.toLowerCase() > a.node.name.toLowerCase() ? -1 : 1);
}