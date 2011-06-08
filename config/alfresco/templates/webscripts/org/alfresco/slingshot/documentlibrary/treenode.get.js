<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Document List Component: treenode
 */
model.treenode = getTreeNode();

/* Create collection of folders in the given space */
function getTreeNode()
{
   try
   {
      var items = new Array(),
         hasSubfolders = true,
         ignoredTypes = ['fm:forum','fm:topic'],
         evalChildFolders = args["children"] !== "false",
         resultsTrimmed = false,
         argMax = parseInt(args["max"], 10),
         maxItems = isNaN(argMax) ? -1 : argMax;
      
      // Use helper function to get the arguments
      var parsedArgs = ParseArgs.getParsedArgs();
      if (parsedArgs === null)
      {
         return;
      }

      // Look for folders in the pathNode
      var folders = parsedArgs.pathNode.childFileFolders(false, true, ignoredTypes);
      for each (item in folders)
      {
         if (evalChildFolders)
         {
               hasSubfolders = item.childFileFolders(false, true, "fm:forum").length > 0;
         }
            
         items.push(
         {
               node: item,
               hasSubfolders: hasSubfolders
         });
            
         if (maxItems !== -1 && items.length > maxItems)
         {
            items.pop();
            resultsTrimmed = true;
            break;
         }
      }
   
      items.sort(sortByName);
   
      return (
      {
         parent: parsedArgs.pathNode,
         resultsTrimmed: resultsTrimmed,
         items: items
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
   return (b.node.name.toLowerCase() > a.node.name.toLowerCase() ? -1 : 1);
}