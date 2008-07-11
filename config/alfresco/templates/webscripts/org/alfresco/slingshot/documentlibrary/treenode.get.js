<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Document List Component: treenode
 */
model.treenode = getTreenode();

/* Create collection of folders in the given space */
function getTreenode(siteId, path)
{
   try
   {
      var items = new Array();
   
      // Use helper function to get the arguments
      var parsedArgs = getParsedArgs();
      if (parsedArgs === null)
      {
         return;
      }

      // Look for folders in the parentNode
      for each(item in parsedArgs.parentNode.children)
      {
         if (item.isContainer)
         {
            items.push(item);
         }
      }
   
      items.sort(sortByName);
   
      return (
      {
         "items": items
      });
   }
   catch(e)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
      return;
   }
}


/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 1);
}