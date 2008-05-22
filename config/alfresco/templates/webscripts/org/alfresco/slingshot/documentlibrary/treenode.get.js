/**
 * Document List Component: treenode
 *
 * Inputs:
 *  mandatory: nodeRef = parent space nodeRef
 *         OR: path = parent space relative path from companyhome
 *
 * Outputs:
 *  treenode - object containing list of child folder nodes for a TreeView widget
 */
model.treenode = getTreenode(args["nodeRef"], args["path"]);

/* Create collection of folders in the given space */
function getTreenode(nodeRef, path)
{
   var items = new Array();
   
   /* nodeRef input */
   var parentSpace = null;
   if ((nodeRef !== null) && (nodeRef != ""))
   {
      parentSpace = search.findNode(nodeRef);
   }
   else if ((path !== null) && path != "")
   {
      parentSpace = companyhome.childByNamePath(path);
   }
   if (parentSpace === null)
   {
      // return jsonError("Parent space nodeRef not supplied");
      parentSpace = companyhome;
   }

   for each(item in parentSpace.children)
   {
      if (item.isContainer)
      {
         items.push(item);
      }
   }
   
   items.sort(sortByName);
   
   return ({
      "items": items
   });
}


/* Format and return error object */
function jsonError(errorString)
{
   var obj =
   {
      "error": errorString
   };
   
   return obj;
}

/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 1);
}